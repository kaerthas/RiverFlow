package com.riverflow.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.riverflow.admin.mapper.SysDeptMapper;
import com.riverflow.admin.service.SysDeptService;
import com.riverflow.api.entity.SysDept;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统部门 Service 实现
 */
@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    @Override
    public List<SysDept> getAllEnabled() {
        return baseMapper.selectAllEnabled();
    }

    @Override
    public Set<Long> getChildDeptIds(Long deptId) {
        if (deptId == null) {
            return Collections.emptySet();
        }
        List<SysDept> allDepts = getAllEnabled();
        Map<Long, List<SysDept>> parentMap = allDepts.stream()
                .collect(Collectors.groupingBy(SysDept::getParentId));

        Set<Long> result = new LinkedHashSet<>();
        Queue<Long> queue = new LinkedList<>();
        queue.offer(deptId);
        while (!queue.isEmpty()) {
            Long current = queue.poll();
            if (result.add(current)) {
                List<SysDept> children = parentMap.get(current);
                if (children != null) {
                    children.forEach(child -> queue.offer(child.getId()));
                }
            }
        }
        return result;
    }
}
