package com.shanjupay.merchant.api;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.api.dto.StaffDTO;
import com.shanjupay.merchant.api.dto.StoreDTO;
import org.springframework.stereotype.Service;

@Service
public interface MerchantService {



    //根据id查询商户
    public MerchantDTO queryMerchantById(Long  id);


    /***
     * 根据租户的id查询商户的信息
     * @param tenantId
     * @return
     */

   public MerchantDTO  queryMerchantByTenantId(Long tenantId);


    /***
     *注册商户服务接口接收账号，密码，手机号
     * @param merchantdto
     * @return注册成功的商户信息
     * @throws BusinessException
     */
    MerchantDTO createMerchant(MerchantDTO merchantdto) throws BusinessException;


    /***
     * 资质申请的接口
     * @param merchantId 商户id
     * @param merchantDTO   资质申请的信息
     * @throws BusinessException
     */

    void applyMerchant(Long merchantId,MerchantDTO merchantDTO) throws BusinessException;


    /***
     * 新增门店
     * @param storeDTO 门店信息
     * @return  新增成功的门店信息
     * @throws BusinessException
     */
    StoreDTO createStore(StoreDTO storeDTO)throws BusinessException;


    /***
     * 新增员工
     * @param staffDTO 员工信息
     * @return  新增成功的员工信息
     * @throws BusinessException
     */
    StaffDTO createStaff(StaffDTO staffDTO) throws BusinessException;


    /***
     * 将员工设置为门店的管理员
     * @param storeId
     * @param staffId
     * @throws BusinessException
     */

    //将员工设置为门店的管理员
    void bindStaffToStore(Long storeId,Long staffId) throws BusinessException;








}
