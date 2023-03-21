package com.inspur.workinfo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("REGIONAL_INSTITUTION")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "区域机构信息")
public class RegionalInstitution extends Model<RegionalInstitution> {
    private static final long serialVersionUID = 1L;
    /**
     * 主键，原系统中具体数据的唯一ID
     */
    @TableId
    @ApiModelProperty(value = "主键，数据的唯一ID，区划编码")
    private String rid;

    @ApiModelProperty(value = "")
    private String pid;

    @ApiModelProperty(value = "机构名称")
    private String orgName;

    @ApiModelProperty(value = "机构所在地名称")
    private String orgShortName;

    @ApiModelProperty(value = "机构编码")
    private String orgCode;

}
