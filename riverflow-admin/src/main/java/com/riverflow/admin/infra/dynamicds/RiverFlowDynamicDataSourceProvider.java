package com.riverflow.admin.infra.dynamicds;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.dynamic.datasource.provider.AbstractJdbcDataSourceProvider;
import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.creator.druid.DruidConfig;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * 动态数据源提供者
 * 从 wf_datasource 表加载数据源配置
 */
@Slf4j
@Component
public class RiverFlowDynamicDataSourceProvider implements DynamicDataSourceProvider {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private StringEncryptor stringEncryptor;

    @Override
    public Map<String, DataSource> loadDataSources() {
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        // 初始加载由 dynamic-datasource 完成，我们提供动态增删能力
        return dataSourceMap;
    }

    /**
     * 创建数据源
     */
    public DataSource createDataSource(String dsCode, String url, String username, String password,
                                        String driverClass, String dbType) {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setName(dsCode);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        // 解密密码
        try {
            if (password != null && password.startsWith("ENC(")) {
                password = stringEncryptor.decrypt(password.substring(4, password.length() - 1));
            }
        } catch (Exception e) {
            log.warn("密码解密失败，使用原始密码: {}", e.getMessage());
        }
        dataSource.setPassword(password);
        if (driverClass != null && !driverClass.isEmpty()) {
            dataSource.setDriverClassName(driverClass);
        } else {
            // 根据数据库类型推断驱动
            dataSource.setDriverClassName(inferDriverClass(dbType));
        }
        dataSource.setInitialSize(5);
        dataSource.setMinIdle(5);
        dataSource.setMaxActive(20);
        dataSource.setMaxWait(60000);
        dataSource.setTestWhileIdle(true);
        dataSource.setValidationQuery("SELECT 1");
        return dataSource;
    }

    /**
     * 测试连接
     */
    public boolean testConnection(String url, String username, String password, String driverClass, String dbType) {
        DataSource dataSource = null;
        try {
            dataSource = createDataSource("test_" + System.currentTimeMillis(), url, username, password, driverClass, dbType);
            if (dataSource instanceof DruidDataSource) {
                ((DruidDataSource) dataSource).init();
                return ((DruidDataSource) dataSource).getConnection() != null;
            }
            return false;
        } catch (Exception e) {
            log.error("数据源连接测试失败: {}", e.getMessage());
            return false;
        } finally {
            if (dataSource instanceof DruidDataSource) {
                ((DruidDataSource) dataSource).close();
            }
        }
    }

    private String inferDriverClass(String dbType) {
        switch (dbType != null ? dbType.toLowerCase() : "mysql") {
            case "oracle":
                return "oracle.jdbc.driver.OracleDriver";
            case "sqlserver":
                return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            case "postgresql":
                return "org.postgresql.Driver";
            case "mysql":
            default:
                return "com.mysql.cj.jdbc.Driver";
        }
    }
}
