package com.riverflow.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.riverflow.admin.service.ApiCatalogService;
import com.riverflow.admin.service.DynamicTableColumnService;
import com.riverflow.admin.service.DynamicTableService;
import com.riverflow.api.entity.ApiCatalog;
import com.riverflow.api.entity.DynamicTable;
import com.riverflow.api.entity.DynamicTableColumn;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private ApiCatalogService apiCatalogService;

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
        dynamicTableColumnService.remove(new QueryWrapper<DynamicTableColumn>().eq("table_id", id));
        for (DynamicTableColumn col : columns) {
            col.setTableId(id);
        }
        dynamicTableColumnService.saveBatch(columns);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        dynamicTableService.removeById(id);
        dynamicTableColumnService.remove(new QueryWrapper<DynamicTableColumn>().eq("table_id", id));
        return R.ok();
    }

    /**
     * 根据动态表配置自动生成对外 SQL 接口
     * 生成 INSERT / SELECT / UPDATE / DELETE 四个接口注册到 wf_api_catalog
     * 外部系统通过 /api/open/{apiCode} 调用
     */
    @PostMapping("/{id}/gen-api")
    public R<Map<String, Object>> generateApi(@PathVariable Long id) {
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
        saveOrUpdateApi(tableCode + "_INSERT", table.getTableName() + "-新增", "sql", "POST", insertSql, dsId);
        generatedApis.add(tableCode + "_INSERT");

        // 2. SELECT 接口（按主键查询）
        String selectSql = "SELECT * FROM " + tableCode + " WHERE " + pkColumn + " = #{" + pkColumn + "}";
        saveOrUpdateApi(tableCode + "_SELECT", table.getTableName() + "-查询", "sql", "GET", selectSql, dsId);
        generatedApis.add(tableCode + "_SELECT");

        // 3. UPDATE 接口
        if (!updatableCols.isEmpty()) {
            String updateSql = buildUpdateSql(tableCode, updatableCols, pkColumn);
            saveOrUpdateApi(tableCode + "_UPDATE", table.getTableName() + "-更新", "sql", "POST", updateSql, dsId);
            generatedApis.add(tableCode + "_UPDATE");
        }

        // 4. DELETE 接口
        String deleteSql = "DELETE FROM " + tableCode + " WHERE " + pkColumn + " = #{" + pkColumn + "}";
        saveOrUpdateApi(tableCode + "_DELETE", table.getTableName() + "-删除", "sql", "POST", deleteSql, dsId);
        generatedApis.add(tableCode + "_DELETE");

        // 5. LIST 接口（分页查询全部）
        String listSql = "SELECT * FROM " + tableCode + " ORDER BY " + pkColumn + " DESC LIMIT #{limit} OFFSET #{offset}";
        saveOrUpdateApi(tableCode + "_LIST", table.getTableName() + "-列表", "sql", "GET", listSql, dsId);
        generatedApis.add(tableCode + "_LIST");

        Map<String, Object> result = new HashMap<>();
        result.put("tableCode", tableCode);
        result.put("generatedApis", generatedApis);
        result.put("invokePrefix", "/api/open/");

        log.info("动态表 [{}] 接口生成完成，共 {} 个接口", tableCode, generatedApis.size());
        return R.ok(result);
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
                                 String method, String sql, Long dsId) {
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
        api.setContentType("application/json");
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
