package com.riverflow.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.riverflow.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 插件管理
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_plugin")
public class SysPlugin extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 插件名称
     */
    private String pluginName;

    /**
     * 插件类型标识
     */
    private String pluginType;

    /**
     * 插件版本
     */
    private String pluginVersion;

    /**
     * 插件分类
     */
    private String category;

    /**
     * 插件描述
     */
    private String description;

    /**
     * JAR包文件名
     */
    private String jarFile;

    /**
     * JAR包存储路径
     */
    private String jarPath;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 图标
     */
    private String icon;

    /**
     * 状态：enabled/disabled
     */
    private String status;

    /**
     * 是否已加载
     */
    private Boolean loaded;

    /**
     * 配置模板JSON
     */
    private String configTemplate;

    /**
     * 作者
     */
    private String author;

    /**
     * 官网/文档地址
     */
    private String website;
}
