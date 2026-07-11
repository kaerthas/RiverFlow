package com.riverflow.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.riverflow.common.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统菜单/权限
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 父菜单ID，0为顶层
     */
    private Long parentId;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 菜单类型：0-目录 1-菜单 2-按钮/API权限
     */
    private Integer menuType;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 路由路径
     */
    private String path;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 权限标识，如 system:user:list
     */
    private String perms;

    /**
     * 显示排序
     */
    private Integer sortNo;

    /**
     * 状态：0-停用 1-启用
     */
    private Integer status;

    /**
     * 子菜单（非持久化字段）
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private List<SysMenu> children = new ArrayList<>();
}
