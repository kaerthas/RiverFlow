package com.riverflow.ai.model.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.riverflow.ai.model.entity.AiModel;
import com.riverflow.ai.model.mapper.AiModelMapper;
import com.riverflow.ai.model.service.AiModelService;
import org.springframework.stereotype.Service;

/**
 * AI 模型配置 Service 实现
 */
@Service
public class AiModelServiceImpl extends ServiceImpl<AiModelMapper, AiModel>
        implements AiModelService {
}
