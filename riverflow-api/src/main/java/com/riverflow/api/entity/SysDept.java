package com.riverflow.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.riverflow.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统部门
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
public class SysDept extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 父部门ID，0为顶层
     */
    private Long parentId;

    /**
     * 部门编码
     */
    private String deptCode;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 负责人
     */
    private String leader;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 显示排序
     */
    private Integer sortNo;

    /**
     * 状态：0-停用 1-启用
     */
    private Integer status;

    /**
     * 子部门（非持久化字段）
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private List<SysDept> children = new ArrayList<>();
}
