package com.shanjupay.merchant.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.merchant.api.AppService;
import com.shanjupay.merchant.api.dto.AppDTO;
import com.shanjupay.merchant.convert.AppConvert;
import com.shanjupay.merchant.entity.App;
import com.shanjupay.merchant.entity.Merchant;
import com.shanjupay.merchant.mapper.AppMapper;
import com.shanjupay.merchant.mapper.MerchantMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

/**
 * @author FreeLoop
 * @date 2022/5/14 17:23
 */


@Service
public class AppServiceImpl implements AppService {
    @Autowired
    AppMapper appMapper;

    @Autowired
    MerchantMapper merchantMapper;

    @Override
    public AppDTO createApp(Long merchantId, AppDTO appDTO) throws BusinessException {

        if (merchantId == null || appDTO == null || StringUtils.isBlank(appDTO.getAppName())) {
            //抛出自定义异常
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        //1.校验商户是否通过资质审核
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(CommonErrorCode.E_200002);
        }
        //取出商户资质申请状态
        String auditStatus = merchant.getAuditStatus();
        if (!"2".equals(auditStatus)) {
            throw new BusinessException(CommonErrorCode.E_200003);
        }

        //应用名称需要校验唯一性
        String appName = appDTO.getAppName();
        Boolean existAppName = isExistAppName(appName);
        if (existAppName) {
            throw new BusinessException(CommonErrorCode.E_200004);
        }


        //2.生成应用id
        String appId = UUID.randomUUID().toString();
        App entity = AppConvert.INSTANCE.dto2entity(appDTO);
        entity.setAppId(appId);
        //调用appMapper向app表插入数据
        appMapper.insert(entity);


        return AppConvert.INSTANCE.entity2dto(entity);
    }

    /***
     * 根据商户id查询应用列表
     * @param merchantId
     * @return
     * @throws BusinessException
     */
    @Override
    public List<AppDTO> queryAppByMerchant(Long merchantId) throws BusinessException {

        List<App> apps = appMapper.selectList(new LambdaQueryWrapper<App>().eq(App::getMerchantId, merchantId));

        return AppConvert.INSTANCE.listentity2dto(apps);
    }


    /***
     *根据应用id查询应用信息
     * @param appId
     * @return
     * @throws BusinessException
     */
    @Override
    public AppDTO getAppById(String appId) throws BusinessException {
        App app = appMapper.selectOne(new LambdaQueryWrapper<App>().eq(App::getAppId, appId));
        return AppConvert.INSTANCE.entity2dto(app);
    }

    //私有方法，判断应用名称是否存在
    private Boolean isExistAppName(String appName) {
        Integer count = appMapper.selectCount(new LambdaQueryWrapper<App>().eq(App::getAppName, appName));
        return count > 0;
    }
}
