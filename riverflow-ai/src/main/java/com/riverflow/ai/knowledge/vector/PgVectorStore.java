package com.riverflow.ai.knowledge.vector;

import com.riverflow.ai.config.AiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * PGVector 向量存储实现
 *
 * <p>复用 PostgreSQL 的 pgvector 扩展。需要数据库已启用 `CREATE EXTENSION vector`。</p>
 */
@Slf4j
@Component
public class PgVectorStore implements VectorStoreProvider {

    public static final String TYPE = "pgvector";

    private final AiProperties aiProperties;
    private final DataSource dataSource;

    @Autowired
    public PgVectorStore(AiProperties aiProperties, DataSource dataSource) {
        this.aiProperties = aiProperties;
        this.dataSource = dataSource;
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void createCollection(String collection, int dimension, DistanceMetric metric) {
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName(collection) + " ("
                + "id VARCHAR(128) PRIMARY KEY,"
                + "doc_id VARCHAR(64),"
                + "chunk_index INT,"
                + "content TEXT,"
                + "metadata JSON,"
                + "embedding vector(" + dimension + ")"
                + ")";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
            createIndex(collection, dimension, metric);
        } catch (SQLException e) {
            throw new RuntimeException("PGVector 创建集合失败: " + collection, e);
        }
    }

    private void createIndex(String collection, int dimension, DistanceMetric metric) throws SQLException {
        String sql = "CREATE INDEX IF NOT EXISTS idx_" + safeName(collection) + "_embedding ON "
                + tableName(collection) + " USING hnsw (embedding " + pgOperator(metric) + ")";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    @Override
    public void upsert(String collection, List<VectorDocument> documents) {
        String sql = "INSERT INTO " + tableName(collection)
                + " (id, doc_id, chunk_index, content, metadata, embedding) VALUES (?, ?, ?, ?, ?::json, ?)"
                + " ON CONFLICT (id) DO UPDATE SET content=EXCLUDED.content, metadata=EXCLUDED.metadata, embedding=EXCLUDED.embedding";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (VectorDocument doc : documents) {
                ps.setString(1, doc.getId());
                ps.setString(2, doc.getDocId());
                ps.setInt(3, doc.getChunkIndex());
                ps.setString(4, doc.getContent());
                ps.setString(5, toJson(doc.getMetadata()));
                ps.setObject(6, toPgVector(doc.getEmbedding()));
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("PGVector upsert 失败: " + collection, e);
        }
    }

    @Override
    public List<VectorDocument> search(String collection, float[] vector, int topK, double minScore) {
        String sql = "SELECT id, doc_id, chunk_index, content, metadata, 1 - (embedding <=> ?) AS score"
                + " FROM " + tableName(collection)
                + " WHERE 1 - (embedding <=> ?) >= ?"
                + " ORDER BY embedding <=> ? LIMIT ?";
        List<VectorDocument> results = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String vec = toPgVector(vector);
            ps.setObject(1, vec);
            ps.setObject(2, vec);
            ps.setDouble(3, minScore);
            ps.setObject(4, vec);
            ps.setInt(5, topK);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VectorDocument doc = new VectorDocument();
                    doc.setId(rs.getString("id"));
                    doc.setCollection(collection);
                    doc.setDocId(rs.getString("doc_id"));
                    doc.setChunkIndex(rs.getInt("chunk_index"));
                    doc.setContent(rs.getString("content"));
                    doc.setScore(rs.getDouble("score"));
                    results.add(doc);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("PGVector 检索失败: " + collection, e);
        }
        return results;
    }

    @Override
    public void deleteByIds(String collection, List<String> ids) {
        String sql = "DELETE FROM " + tableName(collection) + " WHERE id = ANY(?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setArray(1, conn.createArrayOf("varchar", ids.toArray()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("PGVector 删除失败: " + collection, e);
        }
    }

    @Override
    public void dropCollection(String collection) {
        String sql = "DROP TABLE IF EXISTS " + tableName(collection);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("PGVector 删除集合失败: " + collection, e);
        }
    }

    @Override
    public boolean collectionExists(String collection) {
        String sql = "SELECT 1 FROM information_schema.tables WHERE table_name = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName(collection).replaceAll("\"", ""));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("PGVector 检查集合失败: " + collection, e);
        }
    }

    private String tableName(String collection) {
        return "\"ai_vector_" + safeName(collection) + "\"";
    }

    private String safeName(String name) {
        return name.replaceAll("[^a-zA-Z0-9_]", "_");
    }

    private String pgOperator(DistanceMetric metric) {
        return switch (metric) {
            case COSINE -> "vector_cosine_ops";
            case IP -> "vector_ip_ops";
            case L2 -> "vector_l2_ops";
        };
    }

    private String toPgVector(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(vector[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    private String toJson(Object obj) {
        if (obj == null) {
            return "{}";
        }
        try {
            return com.alibaba.fastjson2.JSON.toJSONString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}
