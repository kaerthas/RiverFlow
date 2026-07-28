package com.riverflow.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.riverflow.admin.mapper.ApiAppMapper;
import com.riverflow.admin.service.ApiAppService;
import com.riverflow.api.entity.ApiApp;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ApiAppServiceImpl extends ServiceImpl<ApiAppMapper, ApiApp> implements ApiAppService {

    /**
     * 处理空字符串字段，转换为 NULL（避免唯一键冲突）
     */
    private void normalizeEmptyStrings(ApiApp entity) {
        if (entity != null && !StringUtils.hasText(entity.getAppKey())) {
            entity.setAppKey(null);
        }
    }

    @Override
    public boolean save(ApiApp entity) {
        normalizeEmptyStrings(entity);
        return super.save(entity);
    }

    @Override
    public boolean saveOrUpdate(ApiApp entity) {
        normalizeEmptyStrings(entity);
        return super.saveOrUpdate(entity);
    }

    @Override
    public boolean updateById(ApiApp entity) {
        normalizeEmptyStrings(entity);
        return super.updateById(entity);
    }
}
