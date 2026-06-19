package com.riverflow.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.riverflow.admin.infra.dynamicds.DynamicDataSourceService;
import com.riverflow.admin.service.DatasourceService;
import com.riverflow.api.entity.Datasource;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

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

        // 如果前端未填写密码或传回脱敏值，保持数据库原密码不变
        if (shouldKeepOldPassword(datasource.getPassword())) {
            Datasource old = datasourceService.getById(id);
            if (old != null) {
                datasource.setPassword(old.getPassword());
            }
        } else {
            encryptPasswordIfNeeded(datasource);
        }

        datasourceService.updateById(datasource);
        // 更新后重新加载或移除连接池，确保配置实时生效
        reloadDataSource(datasource);
        return R.ok(datasource.getId());
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        Datasource ds = datasourceService.getById(id);
        if (ds != null) {
            dynamicDataSourceService.removeDataSource(ds.getDsCode());
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
}
