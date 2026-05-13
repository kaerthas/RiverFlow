package com.riverflow.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统操作日志
 */
@Data
@TableName("sys_operation_log")
public class SysOperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String module;

    private String operation;

    private String method;

    private String requestMethod;

    private String requestUrl;

    private String requestParams;

    private Integer responseCode;

    private String responseMsg;

    private String ip;

    private String username;

    private Long executeTime;

    private Integer status;

    private String errorMsg;

    private LocalDateTime createTime;
}
