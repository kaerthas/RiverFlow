package com.riverflow.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.riverflow.admin.infra.dynamicds.DynamicDataSourceService;
import com.riverflow.admin.infra.dynamicds.JdbcDriverJarLoader;
import com.riverflow.admin.infra.dynamicds.JdbcDriverJarValidator;
import com.riverflow.admin.service.DatasourceService;
import com.riverflow.api.entity.Datasource;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * 数据源管理
 */
@Slf4j
@RestController
@RequestMapping("/datasource")
public class DatasourceController {

    @Autowired
    private DatasourceService datasourceService;
    @Autowired
    private DynamicDataSourceService dynamicDataSourceService;
    @Autowired
    private StringEncryptor stringEncryptor;
    @Autowired
    private JdbcDriverJarLoader driverJarLoader;
    @Autowired
    private JdbcDriverJarValidator driverJarValidator;

    /**
     * 密码脱敏占位符，前端编辑时传回该值表示不修改密码
     */
    private static final String PASSWORD_MASK = "******";
    private static final String ENC_PREFIX = "ENC(";
    private static final String ENC_SUFFIX = ")";

    @GetMapping("/list")
    public R<Page<Datasource>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Datasource> pageParam = new Page<>(page, size);
        QueryWrapper<Datasource> qw = new QueryWrapper<>();
        qw.eq("del_flag", 0);
        qw.orderByDesc("create_time");
        Page<Datasource> result = datasourceService.page(pageParam, qw);
        // 列表不返回真实密码
        maskPasswords(result.getRecords());
        return R.ok(result);
    }

    @PostMapping
    public R<Long> save(@RequestBody Datasource datasource) {
        R<Long> validateResult = validateCustomDriver(datasource);
        if (validateResult != null) {
            return validateResult;
        }
        // 新增时密码必填，且加密存储
        encryptPasswordIfNeeded(datasource);
        datasourceService.saveOrUpdate(datasource);
        reloadDataSource(datasource);
        return R.ok(datasource.getId());
    }

    @PutMapping
    public R<Long> update(@RequestBody Datasource datasource) {
        Long id = datasource.getId();
        if (id == null) {
            return R.fail("数据源ID不能为空");
        }

        R<Long> validateResult = validateCustomDriver(datasource);
        if (validateResult != null) {
            return validateResult;
        }

        Datasource old = datasourceService.getById(id);
        if (old == null) {
            return R.fail("数据源不存在");
        }

        // 如果前端未填写密码或传回脱敏值，保持数据库原密码不变
        if (shouldKeepOldPassword(datasource.getPassword())) {
            datasource.setPassword(old.getPassword());
        } else {
            encryptPasswordIfNeeded(datasource);
        }

        // 如果驱动 JAR 路径变更，清理旧的 ClassLoader 和 JAR 文件
        cleanupOldDriverJarIfChanged(old, datasource);

        datasourceService.updateById(datasource);
        reloadDataSource(datasource);
        return R.ok(datasource.getId());
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        Datasource ds = datasourceService.getById(id);
        if (ds != null) {
            dynamicDataSourceService.removeDataSource(ds.getDsCode());
            cleanupDriverJar(ds);
        }
        datasourceService.removeById(id);
        return R.ok();
    }

    @GetMapping("/{id}/test")
    public R<String> testConnection(@PathVariable Long id) {
        Datasource ds = datasourceService.getById(id);
        if (ds == null) return R.fail("数据源不存在");
        boolean success = dynamicDataSourceService.testConnection(ds);
        return success ? R.ok("连接成功") : R.fail("连接失败");
    }

    @PostMapping("/{id}/reload")
    public R<Void> reload(@PathVariable Long id) {
        Datasource ds = datasourceService.getById(id);
        if (ds == null) return R.fail("数据源不存在");
        dynamicDataSourceService.removeDataSource(ds.getDsCode());
        dynamicDataSourceService.addDataSource(ds);
        return R.ok();
    }

    /**
     * 上传 JDBC 驱动 JAR 包
     *
     * @param dsCode      数据源编码
     * @param driverClass 驱动类名
     * @param file        JAR 文件
     * @return 保存后的相对路径
     */
    @PostMapping("/uploadDriverJar")
    public R<String> uploadDriverJar(
            @RequestParam("dsCode") String dsCode,
            @RequestParam("driverClass") String driverClass,
            @RequestParam("file") MultipartFile file) {
        if (!StringUtils.hasText(dsCode)) {
            return R.fail("数据源编码不能为空");
        }
        if (!StringUtils.hasText(driverClass)) {
            return R.fail("驱动类名不能为空");
        }

        R<String> validateResult = driverJarValidator.validate(file, driverClass);
        if (validateResult != null) {
            return validateResult;
        }

        try {
            String jarPath = driverJarLoader.saveJar(dsCode, file);
            return R.ok(jarPath);
        } catch (Exception e) {
            log.error("上传驱动 JAR 失败: {}", e.getMessage(), e);
            return R.fail("上传驱动 JAR 失败: " + e.getMessage());
        }
    }

    /**
     * 重新加载或移除数据源连接池
     */
    private void reloadDataSource(Datasource datasource) {
        if (datasource.getStatus() != null && datasource.getStatus() == 1) {
            try {
                dynamicDataSourceService.removeDataSource(datasource.getDsCode());
                dynamicDataSourceService.addDataSource(datasource);
            } catch (Exception e) {
                log.error("动态加载数据源失败: {}", e.getMessage());
            }
        } else {
            try {
                dynamicDataSourceService.removeDataSource(datasource.getDsCode());
            } catch (Exception e) {
                // 忽略移除不存在的连接池的异常
            }
        }
    }

    /**
     * 对密码进行加密（若尚未加密）
     */
    private void encryptPasswordIfNeeded(Datasource datasource) {
        String password = datasource.getPassword();
        if (!StringUtils.hasText(password)) {
            return;
        }
        if (password.startsWith(ENC_PREFIX) && password.endsWith(ENC_SUFFIX)) {
            // 已经是 ENC(...) 格式，不再重复加密
            return;
        }
        try {
            String encrypted = stringEncryptor.encrypt(password);
            datasource.setPassword(ENC_PREFIX + encrypted + ENC_SUFFIX);
        } catch (Exception e) {
            log.error("数据源密码加密失败: {}", e.getMessage());
            throw new RuntimeException("密码加密失败", e);
        }
    }

    /**
     * 判断是否应保留原密码（前端未修改）
     */
    private boolean shouldKeepOldPassword(String password) {
        return !StringUtils.hasText(password) || PASSWORD_MASK.equals(password);
    }

    /**
     * 对列表中的密码进行脱敏
     */
    private void maskPasswords(List<Datasource> list) {
        if (list == null) {
            return;
        }
        for (Datasource ds : list) {
            if (StringUtils.hasText(ds.getPassword())) {
                ds.setPassword(PASSWORD_MASK);
            }
        }
    }

    /**
     * 驱动 JAR 路径变更时，清理旧的 ClassLoader 和 JAR 文件
     */
    private void cleanupOldDriverJarIfChanged(Datasource oldDs, Datasource newDs) {
        if (!StringUtils.hasText(oldDs.getDriverJarPath())) {
            return;
        }
        String oldPath = oldDs.getDriverJarPath();
        String newPath = newDs.getDriverJarPath();
        if (oldPath.equals(newPath)) {
            return;
        }
        // 路径变更，先移除 ClassLoader，再删除旧文件
        driverJarLoader.removeClassLoader(oldDs.getDsCode());
        deleteJarFile(oldPath);
    }

    /**
     * 清理数据源关联的驱动 JAR 文件
     */
    private void cleanupDriverJar(Datasource ds) {
        if (!StringUtils.hasText(ds.getDriverJarPath())) {
            return;
        }
        driverJarLoader.removeClassLoader(ds.getDsCode());
        deleteJarFile(ds.getDriverJarPath());
    }

    private void deleteJarFile(String jarPath) {
        try {
            Path path = driverJarLoader.resolveJarPath(jarPath);
            Files.deleteIfExists(path);
            log.info("已删除旧驱动 JAR: {}", path);
        } catch (IOException e) {
            log.warn("删除旧驱动 JAR 失败: {}, error={}", jarPath, e.getMessage());
        }
    }

    /**
     * 校验自定义数据库类型的驱动信息是否完整
     */
    private R<Long> validateCustomDriver(Datasource datasource) {
        if (!"other".equalsIgnoreCase(datasource.getDbType())) {
            return null;
        }
        if (!StringUtils.hasText(datasource.getDriverClass())) {
            return R.fail("自定义数据库类型必须填写驱动类名");
        }
        if (!StringUtils.hasText(datasource.getDriverJarPath())) {
            return R.fail("自定义数据库类型必须上传驱动 JAR 包");
        }
        return null;
    }
}
