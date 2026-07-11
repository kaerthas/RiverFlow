package com.riverflow.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.riverflow.api.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统角色 Mapper
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 根据用户ID查询角色列表
     */
    @Select("SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1 AND r.del_flag = 0")
    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);

    /**
     * 根据角色编码查询角色
     */
    @Select("SELECT * FROM sys_role WHERE role_code = #{roleCode} AND del_flag = 0 LIMIT 1")
    SysRole selectByRoleCode(@Param("roleCode") String roleCode);
}
