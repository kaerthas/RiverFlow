package com.riverflow.ai.knowledge.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

/**
 * 接口目录（知识库来源）
 */
@Data
@TableName("wf_api_catalog")
public class ApiCatalog {

    private Long id;
    private String apiCode;
    private String apiName;
    private String apiType;
    private String pluginType;
    private String method;
    private String url;
    private String contentType;
    private String authType;
    private Integer status;
    private Integer delFlag;

    /**
     * 接口参数（不存当前表，由关联查询或知识库索引补充）
     */
    @TableField(exist = false)
    private List<ApiParam> params;
}
