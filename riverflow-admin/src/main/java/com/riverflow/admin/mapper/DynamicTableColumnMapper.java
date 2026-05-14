package com.riverflow.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.riverflow.api.entity.DynamicTableColumn;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DynamicTableColumnMapper extends BaseMapper<DynamicTableColumn> {

    @Select("SELECT * FROM wf_dynamic_table_column WHERE table_id = #{tableId} AND del_flag = 0 ORDER BY sort_no")
    List<DynamicTableColumn> selectByTableId(@Param("tableId") Long tableId);

    /**
     * 物理删除指定表的所有字段配置（绕过 @TableLogic）
     */
    @Delete("DELETE FROM wf_dynamic_table_column WHERE table_id = #{tableId}")
    int physicalDeleteByTableId(@Param("tableId") Long tableId);
}
