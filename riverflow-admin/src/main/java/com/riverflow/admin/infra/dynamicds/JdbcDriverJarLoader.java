package com.riverflow.admin.infra.dynamicds;

import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JDBC 驱动 JAR 加载器
 * <p>
 * 负责管理用户上传的驱动 JAR 包，为每个数据源维护独立的 URLClassLoader，
 * 避免不同版本驱动之间的冲突，并在数据源变更时及时清理旧 ClassLoader。
 */
@Slf4j
@Component
public class JdbcDriverJarLoader {

    /**
     * 驱动 JAR 包存储目录
     */
    @Value("${riverflow.datasource.driver.dir:${user.home}/riverflow/drivers}")
    private String driverDirConfig;

    /**
     * 数据源编码 -> 驱动 JAR 路径缓存
     */
    private final Map<String, String> jarPathCache = new ConcurrentHashMap<>();

    /**
     * 数据源编码 -> URLClassLoader 缓存
     */
    private final Map<String, URLClassLoader> classLoaderCache = new ConcurrentHashMap<>();

    private Path driverDir;

    @PostConstruct
    public void init() {
        try {
            this.driverDir = Paths.get(driverDirConfig).toAbsolutePath().normalize();
            Files.createDirectories(driverDir);
            log.info("JDBC 驱动 JAR 存储目录: {}", driverDir);
        } catch (IOException e) {
            throw new IllegalStateException("无法创建 JDBC 驱动存储目录: " + driverDirConfig, e);
        }
    }

    /**
     * 保存上传的驱动 JAR 文件
     *
     * @param dsCode 数据源编码（用于生成目录隔离）
     * @param file   上传的 JAR 文件
     * @return 保存后的相对路径
     */
    public String saveJar(String dsCode, MultipartFile file) {
        if (!isValidFileName(file.getOriginalFilename())) {
            throw new IllegalArgumentException("驱动 JAR 文件名不合法");
        }
        try {
            Path targetDir = driverDir.resolve(safeFileName(dsCode));
            Files.createDirectories(targetDir);

            String originalFilename = file.getOriginalFilename();
            String fileName = System.currentTimeMillis() + "_" + originalFilename;
            Path targetPath = targetDir.resolve(fileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // 返回相对路径，保留 dsCode 目录层级
            String relativePath = driverDir.relativize(targetPath).toString().replace('\\', '/');
            log.info("驱动 JAR 保存成功: dsCode={}, path={}", dsCode, targetPath);
            return relativePath;
        } catch (IOException e) {
            throw new IllegalStateException("保存驱动 JAR 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取指定数据源和 JAR 路径的 ClassLoader
     *
     * @param dsCode      数据源编码
     * @param jarPath     JAR 相对路径
     * @param driverClass 驱动类名
     * @return URLClassLoader
     */
    public URLClassLoader getClassLoader(String dsCode, String jarPath, String driverClass) {
        if (jarPath == null || jarPath.isEmpty()) {
            return null;
        }

        // 如果 JAR 路径发生变化，关闭旧的 ClassLoader
        String cachedJarPath = jarPathCache.get(dsCode);
        if (cachedJarPath != null && !cachedJarPath.equals(jarPath)) {
            log.info("数据源 {} 的驱动 JAR 路径变更: {} -> {}", dsCode, cachedJarPath, jarPath);
            removeClassLoader(dsCode);
        }

        URLClassLoader existing = classLoaderCache.get(dsCode);
        if (existing != null) {
            return existing;
        }

        synchronized (classLoaderCache) {
            existing = classLoaderCache.get(dsCode);
            if (existing != null) {
                return existing;
            }

            Path jarFile = resolveJarPath(jarPath);
            if (!Files.exists(jarFile)) {
                throw new IllegalArgumentException("驱动 JAR 文件不存在: " + jarFile);
            }

            try {
                URL[] urls = {jarFile.toUri().toURL()};
                URLClassLoader classLoader = new URLClassLoader(urls, JdbcDriverJarLoader.class.getClassLoader());

                // 预先加载驱动类，确保 JAR 中确实包含
                Class<?> driverClazz = Class.forName(driverClass, true, classLoader);
                if (!isDriverClass(driverClazz)) {
                    closeClassLoader(classLoader);
                    throw new IllegalArgumentException("类 " + driverClass + " 不是有效的 JDBC 驱动");
                }

                classLoaderCache.put(dsCode, classLoader);
                jarPathCache.put(dsCode, jarPath);
                log.info("数据源 {} 的 URLClassLoader 创建成功", dsCode);
                return classLoader;
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("驱动 JAR 路径无效: " + jarFile, e);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("在 JAR 中找不到驱动类: " + driverClass, e);
            }
        }
    }

    /**
     * 移除并关闭指定数据源的 ClassLoader
     *
     * @param dsCode 数据源编码
     */
    public void removeClassLoader(String dsCode) {
        URLClassLoader classLoader = classLoaderCache.remove(dsCode);
        jarPathCache.remove(dsCode);
        if (classLoader != null) {
            closeClassLoader(classLoader);
            log.info("数据源 {} 的 URLClassLoader 已关闭", dsCode);
        }
    }

    /**
     * 获取 JAR 文件的绝对路径
     *
     * @param jarPath 相对路径
     * @return 绝对路径
     */
    public Path resolveJarPath(String jarPath) {
        return driverDir.resolve(jarPath).normalize();
    }

    /**
     * 校验文件名是否合法（防止路径遍历）
     */
    private boolean isValidFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        return fileName.toLowerCase().endsWith(".jar") && !fileName.contains("..") && !fileName.contains("/") && !fileName.contains("\\");
    }

    private String safeFileName(String dsCode) {
        return dsCode.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    private boolean isDriverClass(Class<?> clazz) {
        try {
            Class<?> driverClass = Class.forName("java.sql.Driver", true, clazz.getClassLoader());
            return driverClass.isAssignableFrom(clazz);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private void closeClassLoader(URLClassLoader classLoader) {
        try {
            classLoader.close();
        } catch (IOException e) {
            log.warn("关闭 URLClassLoader 失败: {}", e.getMessage());
        }
    }
}
