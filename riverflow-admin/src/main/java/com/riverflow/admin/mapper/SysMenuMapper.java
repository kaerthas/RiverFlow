package com.riverflow.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.riverflow.api.entity.SysMenu;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统菜单/权限 Mapper
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 根据用户ID查询权限标识列表
     */
    @Select("SELECT DISTINCT m.perms FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.status = 1 AND m.del_flag = 0 " +
            "AND m.perms IS NOT NULL AND m.perms != ''")
    List<String> selectPermsByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询菜单列表
     */
    @Select("SELECT DISTINCT m.* FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.status = 1 AND m.del_flag = 0 " +
            "ORDER BY m.sort_no ASC, m.id ASC")
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询权限标识列表
     */
    @Select("SELECT m.perms FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "WHERE rm.role_id = #{roleId} AND m.status = 1 AND m.del_flag = 0 " +
            "AND m.perms IS NOT NULL AND m.perms != ''")
    List<String> selectPermsByRoleId(@Param("roleId") Long roleId);

    /**
     * 物理删除已逻辑删除的菜单（解决 uk_perms 冲突）
     */
    @Delete("DELETE FROM sys_menu WHERE perms = #{perms} AND del_flag = 1")
    int physicalDeleteByPerms(@Param("perms") String perms);
}
