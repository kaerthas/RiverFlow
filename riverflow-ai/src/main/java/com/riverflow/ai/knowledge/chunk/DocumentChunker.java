package com.riverflow.ai.knowledge.chunk;

import java.util.List;

/**
 * 文档分块器抽象
 */
public interface DocumentChunker {

    /**
     * 对文本进行分块
     */
    List<String> chunk(String text, ChunkOptions options);

    /**
     * 使用默认配置分块
     */
    default List<String> chunk(String text) {
        return chunk(text, new ChunkOptions());
    }
}
