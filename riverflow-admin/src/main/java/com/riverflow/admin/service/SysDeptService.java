package com.riverflow.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.riverflow.api.entity.SysDept;

import java.util.List;
import java.util.Set;

/**
 * 系统部门 Service
 */
public interface SysDeptService extends IService<SysDept> {

    /**
     * 查询所有启用部门
     */
    List<SysDept> getAllEnabled();

    /**
     * 查询指定部门及其所有子部门 ID（包含自身）
     *
     * @param deptId 部门ID
     * @return 部门 ID 集合
     */
    Set<Long> getChildDeptIds(Long deptId);
}
