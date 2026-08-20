package com.riverflow.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.riverflow.api.entity.ApiCallLog;

public interface ApiCallLogService extends IService<ApiCallLog> {

    /**
     * 异步保存调用日志，避免阻塞接口主流程
     */
    void saveAsync(ApiCallLog callLog);
}
