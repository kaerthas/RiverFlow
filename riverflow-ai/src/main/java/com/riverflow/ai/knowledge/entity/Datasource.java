package com.riverflow.ai.knowledge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 动态数据源（知识库来源）
 */
@Data
@TableName("wf_datasource")
public class Datasource {

    private Long id;
    private String dsCode;
    private String dsName;
    private String dbType;
    private Integer status;
    private Integer delFlag;
}
