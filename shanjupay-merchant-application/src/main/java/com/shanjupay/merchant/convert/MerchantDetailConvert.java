package com.shanjupay.merchant.convert;

import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.vo.MerchantDetailVO;
import com.shanjupay.merchant.vo.MerchantRegisterVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author FreeLoop
 * @date 2022/5/14 16:21
 */


/**
 * 将商户资质申请的vo和dto进行转换
 *
 * */
@Mapper
public interface MerchantDetailConvert {

    MerchantDetailConvert INSTANCE= Mappers.getMapper(MerchantDetailConvert.class);

    //将dto转换成vo
    MerchantDetailVO dto2vo(MerchantDTO merchantDTO);

    //将vo转换成dto
    MerchantDTO vo2dto(MerchantDetailVO merchantDetailVO);
}
