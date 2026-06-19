package com.riverflow.admin.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.riverflow.admin.infra.dynamicds.DynamicDataSourceService;
import com.riverflow.admin.service.DynamicTableColumnService;
import com.riverflow.admin.service.DynamicTableService;
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
 * 动态表数据通用 CRUD 控制器
 * 根据 wf_dynamic_table / wf_dynamic_table_column 元数据，对物理表进行增删改查
 * 用于在动态表管理页面中直接查看和维护表数据
 */
@Slf4j
@RestController
@RequestMapping("/dynamic-crud")
public class DynamicCrudController {

    @Autowired
    private DynamicTableService dynamicTableService;
    @Autowired
    private DynamicTableColumnService dynamicTableColumnService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private DynamicDataSourceService dynamicDataSourceService;

    /**
     * 获取动态表的列定义
     */
    @GetMapping("/{tableId}/columns")
    public R<List<DynamicTableColumn>> getColumns(@PathVariable Long tableId) {
        DynamicTable table = dynamicTableService.getById(tableId);
        if (table == null) {
            return R.fail("表不存在");
        }
        List<DynamicTableColumn> columns = dynamicTableColumnService.getColumnsByTableId(tableId);
        if (columns == null) {
            columns = new ArrayList<>();
        }
        columns = columns.stream()
                .sorted(Comparator.comparingInt(c -> c.getSortNo() != null ? c.getSortNo() : 0))
                .collect(Collectors.toList());
        return R.ok(columns);
    }

    /**
     * 查询动态表数据（分页）
     * 所有非空请求参数都会作为等值查询条件（主键除外）
     */
    @GetMapping("/{tableId}/data")
    public R<Map<String, Object>> listData(
            @PathVariable Long tableId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam Map<String, String> allParams) {

        DynamicTable table = dynamicTableService.getById(tableId);
        if (table == null) {
            return R.fail("表不存在");
        }

        List<DynamicTableColumn> columns = dynamicTableColumnService.getColumnsByTableId(tableId);
        if (columns == null || columns.isEmpty()) {
            return R.fail("表字段未配置");
        }

        String tableCode = table.getTableCode();
        Long dsId = table.getDsId();

        // 识别主键
        String pkColumn = columns.stream()
                .filter(c -> c.getIsPk() != null && c.getIsPk() == 1)
                .map(DynamicTableColumn::getColumnCode)
                .findFirst()
                .orElse("id");

        // 获取所有可作为查询条件的字段编码
        Set<String> columnCodes = columns.stream()
                .map(DynamicTableColumn::getColumnCode)
                .collect(Collectors.toSet());

        // 构建查询条件
        StringBuilder whereSql = new StringBuilder("WHERE 1=1 ");
        List<Object> args = new ArrayList<>();
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            // 跳过分页参数和空值
            if ("page".equals(key) || "size".equals(key) || value == null || value.trim().isEmpty()) {
                continue;
            }
            if (columnCodes.contains(key)) {
                whereSql.append("AND ").append(escapeIdentifier(key)).append(" = ? ");
                args.add(convertValue(value, columns, key));
            }
        }

        // 查询总数
        String countSql = "SELECT COUNT(*) FROM " + escapeIdentifier(tableCode) + " " + whereSql;
        Long total = executeQueryForObject(dsId, countSql, args.toArray(), Long.class);
        if (total == null) {
            total = 0L;
        }

        // 查询列表
        StringBuilder listSql = new StringBuilder("SELECT * FROM ")
                .append(escapeIdentifier(tableCode))
                .append(" ")
                .append(whereSql)
                .append("ORDER BY ")
                .append(escapeIdentifier(pkColumn))
                .append(" DESC LIMIT ? OFFSET ?");
        args.add(size);
        args.add((page - 1) * size);

        List<Map<String, Object>> list = executeQueryForList(dsId, listSql.toString(), args.toArray());

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", list);
        return R.ok(result);
    }

    /**
     * 获取单条数据详情
     */
    @GetMapping("/{tableId}/data/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long tableId, @PathVariable String id) {
        DynamicTable table = dynamicTableService.getById(tableId);
        if (table == null) {
            return R.fail("表不存在");
        }

        List<DynamicTableColumn> columns = dynamicTableColumnService.getColumnsByTableId(tableId);
        String pkColumn = columns.stream()
                .filter(c -> c.getIsPk() != null && c.getIsPk() == 1)
                .map(DynamicTableColumn::getColumnCode)
                .findFirst()
                .orElse("id");

        String sql = "SELECT * FROM " + escapeIdentifier(table.getTableCode())
                + " WHERE " + escapeIdentifier(pkColumn) + " = ?";
        List<Map<String, Object>> list = executeQueryForList(table.getDsId(), sql, new Object[]{id});
        return R.ok(list.isEmpty() ? null : list.get(0));
    }

    /**
     * 保存数据（新增或更新）
     * 请求体为字段名->值的 Map
     */
    @PostMapping("/{tableId}/data")
    public R<Object> saveData(@PathVariable Long tableId, @RequestBody Map<String, Object> data) {
        DynamicTable table = dynamicTableService.getById(tableId);
        if (table == null) {
            return R.fail("表不存在");
        }

        List<DynamicTableColumn> columns = dynamicTableColumnService.getColumnsByTableId(tableId);
        if (columns == null || columns.isEmpty()) {
            return R.fail("表字段未配置");
        }

        String tableCode = table.getTableCode();
        Long dsId = table.getDsId();

        // 识别主键
        DynamicTableColumn pkCol = columns.stream()
                .filter(c -> c.getIsPk() != null && c.getIsPk() == 1)
                .findFirst()
                .orElse(null);
        String pkColumn = pkCol != null ? pkCol.getColumnCode() : "id";
        Object pkValue = data.get(pkColumn);

        // 过滤出有效字段
        Map<String, DynamicTableColumn> columnMap = columns.stream()
                .collect(Collectors.toMap(DynamicTableColumn::getColumnCode, c -> c, (a, b) -> a));

        boolean isUpdate = pkValue != null && !pkValue.toString().trim().isEmpty();

        if (isUpdate) {
            // 更新：排除主键
            List<String> updateFields = new ArrayList<>();
            List<Object> args = new ArrayList<>();
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String key = entry.getKey();
                if (pkColumn.equals(key)) {
                    continue;
                }
                DynamicTableColumn col = columnMap.get(key);
                if (col == null) {
                    continue;
                }
                updateFields.add(escapeIdentifier(key) + " = ?");
                args.add(formatValueForDb(entry.getValue(), col));
            }
            if (updateFields.isEmpty()) {
                return R.fail("没有需要更新的字段");
            }
            args.add(pkValue);

            String sql = "UPDATE " + escapeIdentifier(tableCode)
                    + " SET " + String.join(", ", updateFields)
                    + " WHERE " + escapeIdentifier(pkColumn) + " = ?";
            int affected = executeUpdate(dsId, sql, args.toArray());
            return R.ok(Collections.singletonMap("affectedRows", affected));
        } else {
            // 新增
            List<String> fieldNames = new ArrayList<>();
            List<Object> args = new ArrayList<>();
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                DynamicTableColumn col = columnMap.get(entry.getKey());
                if (col == null) {
                    continue;
                }
                fieldNames.add(escapeIdentifier(entry.getKey()));
                args.add(formatValueForDb(entry.getValue(), col));
            }
            if (fieldNames.isEmpty()) {
                return R.fail("没有有效的字段数据");
            }

            String sql = "INSERT INTO " + escapeIdentifier(tableCode)
                    + " (" + String.join(", ", fieldNames) + ")"
                    + " VALUES (" + String.join(", ", Collections.nCopies(fieldNames.size(), "?")) + ")";
            int affected = executeUpdate(dsId, sql, args.toArray());
            return R.ok(Collections.singletonMap("affectedRows", affected));
        }
    }

    /**
     * 删除数据
     */
    @DeleteMapping("/{tableId}/data/{id}")
    public R<Object> deleteData(@PathVariable Long tableId, @PathVariable String id) {
        DynamicTable table = dynamicTableService.getById(tableId);
        if (table == null) {
            return R.fail("表不存在");
        }

        List<DynamicTableColumn> columns = dynamicTableColumnService.getColumnsByTableId(tableId);
        String pkColumn = columns.stream()
                .filter(c -> c.getIsPk() != null && c.getIsPk() == 1)
                .map(DynamicTableColumn::getColumnCode)
                .findFirst()
                .orElse("id");

        String sql = "DELETE FROM " + escapeIdentifier(table.getTableCode())
                + " WHERE " + escapeIdentifier(pkColumn) + " = ?";
        int affected = executeUpdate(table.getDsId(), sql, new Object[]{id});
        return R.ok(Collections.singletonMap("affectedRows", affected));
    }

    // ==================== 私有工具方法 ====================

    /**
     * 转义 SQL 标识符（表名、字段名），防止 SQL 注入
     */
    private String escapeIdentifier(String identifier) {
        if (identifier == null) {
            return "";
        }
        return "`" + identifier.replace("`", "``") + "`";
    }

    /**
     * 根据字段类型转换查询参数值
     */
    private Object convertValue(String value, List<DynamicTableColumn> columns, String key) {
        DynamicTableColumn col = columns.stream()
                .filter(c -> c.getColumnCode().equals(key))
                .findFirst()
                .orElse(null);
        if (col == null || col.getDataType() == null) {
            return value;
        }
        String type = col.getDataType().toLowerCase();
        try {
            switch (type) {
                case "int":
                case "integer":
                case "tinyint":
                    return Integer.parseInt(value);
                case "bigint":
                    return Long.parseLong(value);
                case "double":
                    return Double.parseDouble(value);
                case "float":
                    return Float.parseFloat(value);
                case "decimal":
                    return new java.math.BigDecimal(value);
                case "datetime":
                case "timestamp":
                case "date":
                    return java.sql.Timestamp.valueOf(value);
                default:
                    return value;
            }
        } catch (Exception e) {
            return value;
        }
    }

    /**
     * 把前端传过来的值格式化为数据库存储格式
     */
    private Object formatValueForDb(Object value, DynamicTableColumn col) {
        if (value == null) {
            return null;
        }
        String type = col.getDataType() != null ? col.getDataType().toLowerCase() : "varchar";
        if ("json".equals(type)) {
            if (value instanceof String) {
                return value;
            }
            return JSON.toJSONString(value);
        }
        if (value instanceof Map || value instanceof List) {
            return JSON.toJSONString(value);
        }
        return value;
    }

    /**
     * 执行查询（单值）
     */
    @SuppressWarnings("unchecked")
    private <T> T executeQueryForObject(Long dsId, String sql, Object[] args, Class<T> requiredType) {
        try {
            if (dsId == null || dsId == 0) {
                return jdbcTemplate.queryForObject(sql, requiredType, args);
            } else {
                final String execSql = sql;
                final Object[] execArgs = args;
                return (T) dynamicDataSourceService.executeWithDsById(dsId, () ->
                        jdbcTemplate.queryForObject(execSql, requiredType, execArgs));
            }
        } catch (Exception e) {
            log.error("动态CRUD查询失败: sql={}", sql, e);
            throw new RuntimeException("查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行查询（列表）
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> executeQueryForList(Long dsId, String sql, Object[] args) {
        try {
            if (dsId == null || dsId == 0) {
                return jdbcTemplate.queryForList(sql, args);
            } else {
                final String execSql = sql;
                final Object[] execArgs = args;
                return (List<Map<String, Object>>) dynamicDataSourceService.executeWithDsById(dsId, () ->
                        jdbcTemplate.queryForList(execSql, execArgs));
            }
        } catch (Exception e) {
            log.error("动态CRUD查询失败: sql={}", sql, e);
            throw new RuntimeException("查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行更新（INSERT/UPDATE/DELETE）
     */
    private int executeUpdate(Long dsId, String sql, Object[] args) {
        try {
            if (dsId == null || dsId == 0) {
                return jdbcTemplate.update(sql, args);
            } else {
                final String execSql = sql;
                final Object[] execArgs = args;
                return (int) dynamicDataSourceService.executeWithDsById(dsId, () ->
                        jdbcTemplate.update(execSql, execArgs));
            }
        } catch (Exception e) {
            log.error("动态CRUD更新失败: sql={}", sql, e);
            throw new RuntimeException("更新失败: " + e.getMessage(), e);
        }
    }
}
