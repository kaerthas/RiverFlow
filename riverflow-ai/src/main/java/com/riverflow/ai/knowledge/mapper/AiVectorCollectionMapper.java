package com.riverflow.ai.knowledge.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.riverflow.ai.knowledge.entity.AiVectorCollection;
import org.apache.ibatis.annotations.Mapper;

/**
 * 向量集合配置 Mapper
 */
@Mapper
public interface AiVectorCollectionMapper extends BaseMapper<AiVectorCollection> {

    default AiVectorCollection selectByCollection(String collection) {
        LambdaQueryWrapper<AiVectorCollection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiVectorCollection::getCollection, collection);
        return selectOne(wrapper);
    }

    default AiVectorCollection selectDefaultCollection() {
        LambdaQueryWrapper<AiVectorCollection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiVectorCollection::getIsDefault, 1);
        wrapper.orderByDesc(AiVectorCollection::getUpdateTime);
        return selectOne(wrapper);
    }
}
