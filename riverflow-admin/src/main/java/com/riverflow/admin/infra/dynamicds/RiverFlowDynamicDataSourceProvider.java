package com.riverflow.admin.infra.dynamicds;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.dynamic.datasource.provider.AbstractJdbcDataSourceProvider;
import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.creator.druid.DruidConfig;
import com.riverflow.api.enums.DbTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * 动态数据源提供者
 * <p>
 * 支持两种方式加载驱动：
 * 1. 内置数据库类型（MySQL/Oracle/PostgreSQL）使用应用默认 ClassLoader。
 * 2. 自定义数据库类型通过用户上传的 JAR 包创建独立的 URLClassLoader 加载驱动。
 */
@Slf4j
@Component
public class RiverFlowDynamicDataSourceProvider implements DynamicDataSourceProvider {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private StringEncryptor stringEncryptor;
    @Autowired
    private JdbcDriverJarLoader driverJarLoader;

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
        return createDataSource(dsCode, url, username, password, driverClass, dbType, null);
    }

    /**
     * 创建数据源（支持自定义驱动 JAR）
     */
    public DataSource createDataSource(String dsCode, String url, String username, String password,
                                        String driverClass, String dbType, String driverJarPath) {
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

        // 确定驱动类名
        String actualDriverClass = driverClass;
        if (actualDriverClass == null || actualDriverClass.isEmpty()) {
            actualDriverClass = inferDriverClass(dbType);
        }

        if (actualDriverClass == null || actualDriverClass.isEmpty()) {
            throw new IllegalArgumentException("无法确定数据源 " + dsCode + " 的驱动类名，请填写 driverClass");
        }

        // 自定义驱动 JAR 场景：使用独立 URLClassLoader 加载驱动
        if (driverJarPath != null && !driverJarPath.isEmpty()) {
            ClassLoader classLoader = driverJarLoader.getClassLoader(dsCode, driverJarPath, actualDriverClass);
            Driver driver = loadDriver(actualDriverClass, classLoader);
            dataSource.setDriver(driver);
        } else {
            dataSource.setDriverClassName(actualDriverClass);
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
    public boolean testConnection(String url, String username, String password,
                                  String driverClass, String dbType) {
        return testConnection(url, username, password, driverClass, dbType, null);
    }

    /**
     * 测试连接（支持自定义驱动 JAR）
     */
    public boolean testConnection(String url, String username, String password,
                                  String driverClass, String dbType, String driverJarPath) {
        DataSource dataSource = null;
        try {
            dataSource = createDataSource("test_" + System.currentTimeMillis(), url, username,
                    password, driverClass, dbType, driverJarPath);
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

    /**
     * 通过指定 ClassLoader 加载 JDBC 驱动实例
     */
    private Driver loadDriver(String driverClass, ClassLoader classLoader) {
        try {
            Class<?> clazz = Class.forName(driverClass, true, classLoader);
            Object instance = clazz.getDeclaredConstructor().newInstance();
            if (!(instance instanceof Driver)) {
                throw new IllegalArgumentException("类 " + driverClass + " 不是 JDBC 驱动");
            }
            return (Driver) instance;
        } catch (Exception e) {
            throw new IllegalArgumentException("加载驱动失败: " + driverClass, e);
        }
    }

    private String inferDriverClass(String dbType) {
        DbTypeEnum type = DbTypeEnum.fromCode(dbType);
        return type == DbTypeEnum.OTHER ? null : type.getDriverClass();
    }
}
