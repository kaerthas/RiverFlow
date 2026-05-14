package com.riverflow.admin.infra.dynamicds;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.riverflow.admin.service.DatasourceService;
import com.riverflow.api.entity.Datasource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * 动态数据源管理服务
 */
@Slf4j
@Service
public class DynamicDataSourceService {

    @Autowired
    private DatasourceService datasourceService;
    @Autowired
    private RiverFlowDynamicDataSourceProvider dataSourceProvider;
    @Autowired
    private DynamicRoutingDataSource dynamicRoutingDataSource;

    /**
     * 应用启动时加载所有启用的数据源
     */
    @PostConstruct
    public void init() {
        log.info("初始化动态数据源...");
        List<Datasource> list = datasourceService.list();
        for (Datasource ds : list) {
            if (ds.getStatus() != null && ds.getStatus() == 1) {
                try {
                    addDataSource(ds);
                    log.info("数据源加载成功: {}", ds.getDsCode());
                } catch (Exception e) {
                    log.error("数据源加载失败: {}, error={}", ds.getDsCode(), e.getMessage());
                }
            }
        }
    }

    /**
     * 添加数据源到连接池
     */
    public void addDataSource(Datasource ds) {
        DataSource dataSource = dataSourceProvider.createDataSource(
                ds.getDsCode(), ds.getUrl(), ds.getUsername(),
                ds.getPassword(), ds.getDriverClass(), ds.getDbType());
        // 使用 dynamic-datasource 的公共API添加
        dynamicRoutingDataSource.addDataSource(ds.getDsCode(), dataSource);
    }

    /**
     * 移除数据源
     */
    public void removeDataSource(String dsCode) {
        dynamicRoutingDataSource.removeDataSource(dsCode);
    }

    /**
     * 测试连接
     */
    public boolean testConnection(Datasource ds) {
        return dataSourceProvider.testConnection(ds.getUrl(), ds.getUsername(),
                ds.getPassword(), ds.getDriverClass(), ds.getDbType());
    }

    /**
     * 切换数据源并执行SQL
     */
    public Object executeWithDs(String dsCode, SqlExecutor executor) throws Exception {
        DynamicDataSourceContextHolder.push(dsCode);
        try {
            return executor.execute();
        } finally {
            DynamicDataSourceContextHolder.poll();
        }
    }

    @FunctionalInterface
    public interface SqlExecutor {
        Object execute() throws Exception;
    }
}
