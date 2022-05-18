package com.shanjupay.merchant.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author 嘉宝
 * @date 2022/5/14 15:55
 */
@ApiModel(value = "MerchantDetailVO", description = "商户资质申请信息")
@Data
public class MerchantDetailVO implements Serializable {
    @ApiModelProperty("企业名称")
    private String merchantName;
    @ApiModelProperty("企业编号")
    private String merchantNo;
    @ApiModelProperty("企业地址")
    private String merchantAddress;
    @ApiModelProperty("行业类型")
    private String merchantType;
    @ApiModelProperty("营业执照")
    private String businessLicensesImg;
    @ApiModelProperty("法人身份证正面")
    private String idCardFrontImg;
    @ApiModelProperty("法人身份证反面")
    private String idCardAfterImg;
    @ApiModelProperty("联系人")
    private String username;
    @ApiModelProperty("联系人地址")
    private String contactsAddress;

}
