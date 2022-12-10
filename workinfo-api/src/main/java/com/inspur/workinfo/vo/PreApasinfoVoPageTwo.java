package com.inspur.workinfo.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 登记（申报）信息
 *
 * @author Jason
 * @date 2020-06-17 10:15:05
 */
@Data
@ApiModel(value = "登记（申报）信息请求")
public class PreApasinfoVoPageTwo {
    private static final long serialVersionUID = 1L;

    /**
     * 主键，详见附录2.3
     */
    @ApiModelProperty(value = "主键，详见附录2.3")
    private String projid;

    /**
     * 详见附录2.1
     */
    @ApiModelProperty(value = "详见附录2.1")
    private String itemCode;
    /**
     * 详见附录2.2
     */
    @ApiModelProperty(value = "详见附录2.2")
    private String implementCode;
    /**
     * 审批事项的版本号
     */
    @ApiModelProperty(value = "审批事项的版本号")
    private String itemversion;
    /**
     * 申报的事项名称，如：交通建设工程施工许可
     */
    @ApiModelProperty(value = "申报的事项名称，如：交通建设工程施工许可")
    private String itemname;
    /**
     * 申请审批的项目的具体名称。如：关于XXX的交通建设工程施工许可
     */
    @ApiModelProperty(value = "申请审批的项目的具体名称。如：关于XXX的交通建设工程施工许可")
    private String projectname;
    /**
     * 详见附录2.7
     */
    @ApiModelProperty(value = "详见附录2.7")
    private Integer projectstate;
    /**
     * 1-即办件，2-承诺件，3-联办件，4-上报件。
     */
    @ApiModelProperty(value = "1-即办件，2-承诺件，3-联办件，4-上报件。")
    private Integer infotype;
    /**
     * GB/T 19488.2-2008中姓名或
     * 机构名称。
     */
    @ApiModelProperty(value = "GB/T 19488.2-2008中姓名或机构名称")
    private String applyname;
    /**
     * GB/T 19488.2-2008中身份证件号码
     */
    @ApiModelProperty(value = "GB/T 19488.2-2008中身份证件号码")
    private String applyCardtypenumber;
    /**
     * GB/T 19488.2-2008中姓名。
     */
    @ApiModelProperty(value = "GB/T 19488.2-2008中姓名。")
    private String contactman;
    /**
     * GB/T 19488.2-2008中身份证件号码
     */
    @ApiModelProperty(value = "GB/T 19488.2-2008中身份证件号码")
    private String contactmanCardnumber;


    /**
     * GB/T 19488.2-2008中法定代表人
     */
    @ApiModelProperty(value = "GB/T 19488.2-2008中法定代表人")
    private String legalman;
    /**
     * GB 32100-2015中统一社会信用代码。
     */
    @ApiModelProperty(value = "GB 32100-2015中统一社会信用代码。")
    private String deptid;
    /**
     * GB/T 19488.2-2008中机构名称。
     */
    @ApiModelProperty(value = "GB/T 19488.2-2008中机构名称。")
    private String deptname;

    /**
     * 创建用户名称
     */
    @ApiModelProperty(value = "创建用户名称")
    private String receiveName;
    /**
     * GB/T 19488.2-2008中时间。
     */
    @ApiModelProperty(value = "由各业务系统产生，时间格式：YYYY-MM-DD")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date receivetime;

    /**
     * 编码规则见附录2.4
     */
    @ApiModelProperty(value = "编码规则见附录2.4")
    private String regionId;
    /**
     * 0-作废，1-有效。
     */
    @ApiModelProperty(value = "0-作废，1-有效。")
//    @TableLogic(value="1",delval = "0")
    private Integer datastate;
    /**
     * 由各业务系统产生，时间格式：YYYY-MM-DDHH24:MI:SS。
     */
    @ApiModelProperty(value = "由各业务系统产生，时间格式：YYYY-MM-DD HH24:MI:SS。")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime;



    /**
     * 系统标记
     */
    @ApiModelProperty(value = "系统标记")
    private String sysmark;

    /**
     * 申请人证件类型
     */
    @ApiModelProperty(value = "申请人证件类型")
    private String applyCardtype;
    /**
     * 申请来源 办理形式（国家）
     */
    @ApiModelProperty(value = "申请来源")
    private String applyfrom;
    /**
     * 联系人证件类对象
     */
    @ApiModelProperty(value = "联系人证件类型")
    private String contactmanCardtype;
    /**
     * 事项ID
     */
    @ApiModelProperty(value = "事项ID")
    private String sourceid;
    @ApiModelProperty(value = "当前阶段,projectstateType 1-受理中，2-已办结，3-已撤销,null-所有")
    @TableField(exist = false)
    private String projectstateType;

    /**
     * 事项类型
     */
    @ApiModelProperty(value="事项类型")
    private String itemType;
}
