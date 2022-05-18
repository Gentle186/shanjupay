package com.shanjupay.merchant.api;

/**
 * @author FreeLoop
 * @date 2022/5/14 17:18
 */

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.merchant.api.dto.AppDTO;

import java.util.List;

/***
 * 应用管理相关的接口
 */
public interface AppService {

    /***
     *  创建应用的接口
     * @param merchantId 商户id
     * @param appDTO 应用的信息
     * @return  返回创建成功的应用信息
     * @throws BusinessException
     */
    AppDTO createApp(Long merchantId,AppDTO appDTO) throws BusinessException;

    /***
     * 根据商户id查询应用列表
     * @param merchantId
     * @return
     * @throws BusinessException
     */
    List<AppDTO> queryAppByMerchant(Long merchantId) throws BusinessException;


    /***
     * 根据应用id查询应用信息
     * @param appId
     * @return
     * @throws BusinessException
     */
    AppDTO getAppById(String appId) throws BusinessException;


}
