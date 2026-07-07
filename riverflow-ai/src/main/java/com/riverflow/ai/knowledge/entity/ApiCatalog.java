package com.riverflow.ai.knowledge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

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
    private String method;
    private String url;
    private String contentType;
    private Integer status;
    private Integer delFlag;
}
