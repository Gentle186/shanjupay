package com.shanjupay.merchant.convert;


import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.vo.MerchantRegisterVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 将商户注册的vo和dto进行转换
 *
 * */

@Mapper
public interface MerchantRegisterConvert {


    MerchantRegisterConvert INSTANCE= Mappers.getMapper(MerchantRegisterConvert.class);

    //将dto转换成vo
    MerchantRegisterVO dto2vo(MerchantDTO merchantDTO);

    //将vo转换成dto
    MerchantDTO vo2dto(MerchantRegisterVO merchantRegisterVO);

}
