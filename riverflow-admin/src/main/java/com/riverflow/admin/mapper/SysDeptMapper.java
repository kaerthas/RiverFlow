package com.riverflow.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.riverflow.api.entity.SysDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统部门 Mapper
 */
@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {

    /**
     * 查询所有启用部门
     */
    @Select("SELECT * FROM sys_dept WHERE status = 1 AND del_flag = 0 ORDER BY sort_no, id")
    List<SysDept> selectAllEnabled();

    /**
     * 根据部门编码查询
     */
    @Select("SELECT * FROM sys_dept WHERE dept_code = #{deptCode} AND del_flag = 0 LIMIT 1")
    SysDept selectByDeptCode(@Param("deptCode") String deptCode);
}
