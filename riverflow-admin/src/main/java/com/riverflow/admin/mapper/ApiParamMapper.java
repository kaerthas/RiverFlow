package com.riverflow.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.riverflow.api.entity.ApiParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ApiParamMapper extends BaseMapper<ApiParam> {

    @Select("SELECT * FROM wf_api_param WHERE api_id = #{apiId} AND del_flag = 0 ORDER BY sort_no")
    List<ApiParam> selectByApiId(@Param("apiId") Long apiId);
}
