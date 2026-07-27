package com.riverflow.ai.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * AI 模块查询已启用节点插件类型
 *
 * <p>与 admin 共用同一张 sys_plugin 表，仅查询 AI 流程校验所需的节点插件 nodeType。</p>
 */
@Mapper
public interface SysPluginTypeMapper {

    /**
     * 查询所有已启用且作用域包含节点插件的 plugin_type
     */
    @Select("SELECT DISTINCT plugin_type FROM sys_plugin " +
            "WHERE status = 'enabled' AND del_flag = 0 " +
            "AND (plugin_scope IS NULL OR plugin_scope IN ('node', 'both'))")
    List<String> selectNodePluginTypes();
}
