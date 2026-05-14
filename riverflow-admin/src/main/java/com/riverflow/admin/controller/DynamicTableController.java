package com.riverflow.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.riverflow.admin.infra.dynamicds.DynamicDataSourceService;
import com.riverflow.admin.mapper.DynamicTableColumnMapper;
import com.riverflow.admin.service.ApiCatalogService;
import com.riverflow.admin.service.DynamicTableColumnService;
import com.riverflow.admin.service.DynamicTableService;
import com.riverflow.api.entity.ApiCatalog;
import com.riverflow.api.entity.DynamicTable;
import com.riverflow.api.entity.DynamicTableColumn;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 动态表管理
 * 支持通过表配置自动生成对外暴露的 SQL 接口
 */
@Slf4j
@RestController
@RequestMapping("/dynamic-table")
public class DynamicTableController {

    @Autowired
    private DynamicTableService dynamicTableService;
    @Autowired
    private DynamicTableColumnService dynamicTableColumnService;
    @Autowired
    private DynamicTableColumnMapper dynamicTableColumnMapper;
    @Autowired
    private ApiCatalogService apiCatalogService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private DynamicDataSourceService dynamicDataSourceService;

    @GetMapping("/list")
    public R<Page<DynamicTable>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<DynamicTable> pageParam = new Page<>(page, size);
        QueryWrapper<DynamicTable> qw = new QueryWrapper<>();
        qw.eq("del_flag", 0);
        qw.orderByDesc("create_time");
        return R.ok(dynamicTableService.page(pageParam, qw));
    }

    @GetMapping("/{id}")
    public R<DynamicTable> getById(@PathVariable Long id) {
        return R.ok(dynamicTableService.getById(id));
    }

    @GetMapping("/{id}/columns")
    public R<List<DynamicTableColumn>> getColumns(@PathVariable Long id) {
        return R.ok(dynamicTableColumnService.getColumnsByTableId(id));
    }

    @PostMapping
    public R<Long> save(@RequestBody DynamicTable table) {
        dynamicTableService.saveOrUpdate(table);
        return R.ok(table.getId());
    }

    @PostMapping("/{id}/columns")
    public R<Void> saveColumns(@PathVariable Long id, @RequestBody List<DynamicTableColumn> columns) {
        // 物理删除旧字段配置（绕过 @TableLogic，避免唯一键冲突）
        dynamicTableColumnMapper.physicalDeleteByTableId(id);
        for (DynamicTableColumn col : columns) {
            col.setId(null);
            col.setTableId(id);
        }
        dynamicTableColumnService.saveBatch(columns);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        dynamicTableService.removeById(id);
        // 物理删除关联字段配置（绕过 @TableLogic）
        dynamicTableColumnMapper.physicalDeleteByTableId(id);
        return R.ok();
    }

    /**
     * 根据动态表配置自动生成对外 SQL 接口
     * 生成 INSERT / SELECT / UPDATE / DELETE 四个接口注册到 wf_api_catalog
     * 外部系统通过 /open/{apiCode} 调用
     */
    @PostMapping("/{id}/gen-api")
    public R<Map<String, Object>> generateApi(@PathVariable Long id,
                                              @RequestParam(defaultValue = "application/json") String contentType) {
        DynamicTable table = dynamicTableService.getById(id);
        if (table == null) {
            return R.fail("表不存在");
        }

        List<DynamicTableColumn> columns = dynamicTableColumnService.getColumnsByTableId(id);
        if (columns == null || columns.isEmpty()) {
            return R.fail("表字段未配置，无法生成接口");
        }

        String tableCode = table.getTableCode();
        Long dsId = table.getDsId();

        // 按 sortNo 排序
        List<DynamicTableColumn> sortedCols = columns.stream()
                .sorted(Comparator.comparingInt(c -> c.getSortNo() != null ? c.getSortNo() : 0))
                .collect(Collectors.toList());

        // 主键字段
        String pkColumn = sortedCols.stream()
                .filter(c -> c.getIsPk() != null && c.getIsPk() == 1)
                .map(DynamicTableColumn::getColumnCode)
                .findFirst()
                .orElse("id");

        // 非主键字段（用于 INSERT）
        List<DynamicTableColumn> nonPkCols = sortedCols.stream()
                .filter(c -> c.getIsPk() == null || c.getIsPk() != 1)
                .collect(Collectors.toList());

        // 可更新字段（非主键）
        List<DynamicTableColumn> updatableCols = nonPkCols.stream()
                .filter(c -> !"create_time".equals(c.getColumnCode()) && !"create_by".equals(c.getColumnCode()))
                .collect(Collectors.toList());

        List<String> generatedApis = new ArrayList<>();

        // 1. INSERT 接口
        String insertSql = buildInsertSql(tableCode, nonPkCols);
        saveOrUpdateApi(tableCode + "_INSERT", table.getTableName() + "-新增", "sql", "POST", insertSql, dsId, contentType);
        generatedApis.add(tableCode + "_INSERT");

        // 2. SELECT 接口（按主键查询）
        String selectSql = "SELECT * FROM " + tableCode + " WHERE " + pkColumn + " = #{" + pkColumn + "}";
        saveOrUpdateApi(tableCode + "_SELECT", table.getTableName() + "-查询", "sql", "GET", selectSql, dsId, contentType);
        generatedApis.add(tableCode + "_SELECT");

        // 3. UPDATE 接口
        if (!updatableCols.isEmpty()) {
            String updateSql = buildUpdateSql(tableCode, updatableCols, pkColumn);
            saveOrUpdateApi(tableCode + "_UPDATE", table.getTableName() + "-更新", "sql", "POST", updateSql, dsId, contentType);
            generatedApis.add(tableCode + "_UPDATE");
        }

        // 4. DELETE 接口
        String deleteSql = "DELETE FROM " + tableCode + " WHERE " + pkColumn + " = #{" + pkColumn + "}";
        saveOrUpdateApi(tableCode + "_DELETE", table.getTableName() + "-删除", "sql", "POST", deleteSql, dsId, contentType);
        generatedApis.add(tableCode + "_DELETE");

        // 5. LIST 接口（分页查询全部）
        String listSql = "SELECT * FROM " + tableCode + " ORDER BY " + pkColumn + " DESC LIMIT #{limit} OFFSET #{offset}";
        saveOrUpdateApi(tableCode + "_LIST", table.getTableName() + "-列表", "sql", "GET", listSql, dsId, contentType);
        generatedApis.add(tableCode + "_LIST");

        Map<String, Object> result = new HashMap<>();
        result.put("tableCode", tableCode);
        result.put("generatedApis", generatedApis);
        result.put("invokePrefix", "/open/");
        result.put("contentType", contentType);

        log.info("动态表 [{}] 接口生成完成，共 {} 个接口，contentType={}", tableCode, generatedApis.size(), contentType);
        return R.ok(result);
    }

    /**
     * 根据动态表元数据自动创建物理表（执行 DDL）
     */
    @PostMapping("/{id}/create-table")
    public R<Object> createPhysicalTable(@PathVariable Long id) {
        DynamicTable table = dynamicTableService.getById(id);
        if (table == null) {
            return R.fail("表不存在");
        }

        List<DynamicTableColumn> columns = dynamicTableColumnService.getColumnsByTableId(id);
        if (columns == null || columns.isEmpty()) {
            return R.fail("表字段未配置，无法创建物理表");
        }

        String ddl = buildDdl(table, columns);
        Long dsId = table.getDsId();

        try {
            if (dsId == null || dsId == 0) {
                jdbcTemplate.execute(ddl);
            } else {
                dynamicDataSourceService.executeWithDsById(dsId, () -> {
                    jdbcTemplate.execute(ddl);
                    return null;
                });
            }
            log.info("动态表 [{}] 物理表创建成功", table.getTableCode());
            Map<String, String> result = new HashMap<>();
            result.put("msg", "物理表创建成功: " + table.getTableCode());
            result.put("ddl", ddl);
            return R.ok(result);
        } catch (Exception e) {
            log.error("动态表 [{}] 物理表创建失败", table.getTableCode(), e);
            return R.fail("物理表创建失败: " + e.getMessage());
        }
    }

    /**
     * 根据元数据构建 CREATE TABLE DDL
     */
    private String buildDdl(DynamicTable table, List<DynamicTableColumn> columns) {
        StringBuilder ddl = new StringBuilder();
        ddl.append("CREATE TABLE IF NOT EXISTS ").append(table.getTableCode()).append(" (\n");

        List<DynamicTableColumn> sortedCols = columns.stream()
                .sorted(Comparator.comparingInt(c -> c.getSortNo() != null ? c.getSortNo() : 0))
                .collect(Collectors.toList());

        for (int i = 0; i < sortedCols.size(); i++) {
            DynamicTableColumn col = sortedCols.get(i);
            ddl.append("  ").append(col.getColumnCode()).append(" ")
               .append(buildColumnType(col));

            if (col.getIsRequired() != null && col.getIsRequired() == 1) {
                ddl.append(" NOT NULL");
            }
            if (col.getDefaultValue() != null && !col.getDefaultValue().isEmpty()) {
                ddl.append(" DEFAULT '").append(col.getDefaultValue()).append("'");
            }
            if (col.getIsPk() != null && col.getIsPk() == 1) {
                ddl.append(" PRIMARY KEY");
            }
            if (i < sortedCols.size() - 1) {
                ddl.append(",");
            }
            ddl.append(" COMMENT '").append(col.getColumnName()).append("'\n");
        }

        ddl.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci")
           .append(" COMMENT '").append(table.getTableName()).append("';");

        return ddl.toString();
    }

    /**
     * 构建字段类型定义
     */
    private String buildColumnType(DynamicTableColumn col) {
        String type = col.getDataType() != null ? col.getDataType().toLowerCase() : "varchar";
        Integer length = col.getLength();
        Integer scale = col.getDecimalScale();

        switch (type) {
            case "varchar":
                return "VARCHAR(" + (length != null && length > 0 ? length : 255) + ")";
            case "int":
            case "integer":
                return "INT";
            case "bigint":
                return "BIGINT";
            case "datetime":
            case "timestamp":
                return "DATETIME";
            case "date":
                return "DATE";
            case "text":
                return "TEXT";
            case "longtext":
                return "LONGTEXT";
            case "decimal":
                int p = length != null && length > 0 ? length : 18;
                int s = scale != null && scale >= 0 ? scale : 2;
                return "DECIMAL(" + p + "," + s + ")";
            case "json":
                return "JSON";
            case "tinyint":
                return "TINYINT";
            case "double":
                return "DOUBLE";
            case "float":
                return "FLOAT";
            default:
                return "VARCHAR(" + (length != null && length > 0 ? length : 255) + ")";
        }
    }

    /**
     * 构建 INSERT SQL
     */
    private String buildInsertSql(String tableCode, List<DynamicTableColumn> columns) {
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableCode).append(" (");
        StringBuilder values = new StringBuilder("VALUES (");

        for (int i = 0; i < columns.size(); i++) {
            DynamicTableColumn col = columns.get(i);
            if (i > 0) {
                sql.append(", ");
                values.append(", ");
            }
            sql.append(col.getColumnCode());
            values.append("#{").append(col.getColumnCode()).append("}");
        }

        sql.append(") ").append(values).append(")");
        return sql.toString();
    }

    /**
     * 构建 UPDATE SQL
     */
    private String buildUpdateSql(String tableCode, List<DynamicTableColumn> columns, String pkColumn) {
        StringBuilder sql = new StringBuilder("UPDATE ").append(tableCode).append(" SET ");
        for (int i = 0; i < columns.size(); i++) {
            DynamicTableColumn col = columns.get(i);
            if (i > 0) sql.append(", ");
            sql.append(col.getColumnCode()).append(" = #{").append(col.getColumnCode()).append("}");
        }
        sql.append(" WHERE ").append(pkColumn).append(" = #{").append(pkColumn).append("}");
        return sql.toString();
    }

    /**
     * 保存或更新接口配置到 wf_api_catalog
     * SQL 语句存储在 url 字段中
     */
    private void saveOrUpdateApi(String apiCode, String apiName, String apiType,
                                 String method, String sql, Long dsId, String contentType) {
        ApiCatalog api = apiCatalogService.getOne(
                new QueryWrapper<ApiCatalog>().eq("api_code", apiCode)
        );
        if (api == null) {
            api = new ApiCatalog();
        }
        api.setApiCode(apiCode);
        api.setApiName(apiName);
        api.setApiType(apiType);
        api.setMethod(method);
        api.setUrl(sql);
        api.setContentType(contentType);
        api.setAuthType("none");
        api.setDsId(dsId);
        api.setTimeout(30000);
        api.setRetryTimes(0);
        api.setProxyEnabled(0);
        api.setStatus(1);
        api.setDelFlag(0);
        apiCatalogService.saveOrUpdate(api);
    }
}
