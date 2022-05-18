package com.shanjupay.merchant.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;

import com.alibaba.fastjson.JSON;
import com.alipay.api.domain.IdCardImg;
import com.alipay.api.domain.UserName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.domain.ErrorCode;
import com.shanjupay.common.util.PhoneUtil;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.api.dto.StaffDTO;
import com.shanjupay.merchant.api.dto.StoreDTO;
import com.shanjupay.merchant.convert.MerchantConvert;
import com.shanjupay.merchant.convert.StaffConvert;
import com.shanjupay.merchant.convert.StoreConvert;
import com.shanjupay.merchant.entity.Merchant;
import com.shanjupay.merchant.entity.Staff;
import com.shanjupay.merchant.entity.Store;
import com.shanjupay.merchant.entity.StoreStaff;
import com.shanjupay.merchant.mapper.MerchantMapper;
import com.shanjupay.merchant.mapper.StaffMapper;
import com.shanjupay.merchant.mapper.StoreMapper;
import com.shanjupay.merchant.mapper.StoreStaffMapper;
import com.shanjupay.user.api.TenantService;
import com.shanjupay.user.api.dto.tenant.CreateTenantRequestDTO;
import com.shanjupay.user.api.dto.tenant.TenantDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.security.auth.login.Configuration;

@Service
@Slf4j
public class MerchantServiceImpl implements MerchantService {


    @Autowired
    MerchantMapper merchantMapper;
    @Autowired
    StoreMapper storeMapper;
    @Autowired
    StaffMapper staffMapper;
    @Autowired
    StoreStaffMapper storeStaffMapper;
    @Reference  //远程调用dubbo接口
    TenantService tenantService;

    @Override
    public MerchantDTO queryMerchantById(Long id) {

        Merchant merchant = merchantMapper.selectById(id);
     /*   MerchantDTO merchantDTO = new MerchantDTO();
        merchantDTO.setId(merchant.getId());
        merchantDTO.setMerchantName(merchant.getMerchantName());*/
        return MerchantConvert.INSTANCE.entity2dto(merchant);
    }


    /***
     * 根据租户的id查询商户的信息
     * @param tenantId
     * @return
     */
    @Override
    public MerchantDTO queryMerchantByTenantId(String tenantId) {
        Merchant merchant = merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>().eq(Merchant::getTenantId, tenantId));
        return MerchantConvert.INSTANCE.entity2dto(merchant);
    }


    /***
     * 注册商户服务接口，接收账号密码，手机号为了可扩展性使用merchantDTO接收数据
     * 调用saas接口新增租户，用户，绑定租户与用户的关系,初始化权限
     * @param merchantDTO 商户注册信息
     * @return 注册成功的商户信息
     * @throws BusinessException
     */
    @Override
    public MerchantDTO createMerchant(MerchantDTO merchantDTO) throws BusinessException {

        //校验参数的合法性
        if (merchantDTO == null) {
            throw new BusinessException(CommonErrorCode.E_100108);
        }
        if (StringUtils.isBlank(merchantDTO.getMobile())) {
            throw new BusinessException(CommonErrorCode.E_100112);
        }
        if (StringUtils.isBlank(merchantDTO.getPassword())) {
            throw new BusinessException(CommonErrorCode.E_100111);
        }
        if (!PhoneUtil.isMatches(merchantDTO.getMobile())) {
            throw new BusinessException(CommonErrorCode.E_100109);
        }

        //校验手机号的唯一性
        //根据手机号查询商户表，如果有记录则说明手机号已经存在
        Integer count = merchantMapper.selectCount(new LambdaQueryWrapper<Merchant>().eq(Merchant::getMobile, merchantDTO.getMobile()));
        if (count > 0) {
            throw new BusinessException(CommonErrorCode.E_100113);
        }

        //调用saas接口
        //构建调用的参数
        CreateTenantRequestDTO createTenantRequestDTO = new CreateTenantRequestDTO();
        createTenantRequestDTO.setMobile(merchantDTO.getMobile());
        createTenantRequestDTO.setUsername(merchantDTO.getUsername());
        createTenantRequestDTO.setPassword(merchantDTO.getPassword());
        createTenantRequestDTO.setTenantTypeCode("shanju-merchant");    //租户类型
        createTenantRequestDTO.setBundleCode("shanju-merchant");    //套餐，根据套餐分配权限
        createTenantRequestDTO.setName(merchantDTO.getUsername());  //租户名称，跟账户名称一样

        //如果租户的saas已经存在,Saas直接返回此租户的信息，否则添加
        TenantDTO tenantAndAccount = tenantService.createTenantAndAccount(createTenantRequestDTO);
        //获取租户的id
        if (tenantAndAccount==null||tenantAndAccount.getId()==null){
            throw new BusinessException(CommonErrorCode.E_200012);
        }

        //租户的id
        Long tenantId = tenantAndAccount.getId();

        //租户id在商户表的唯一
        //根据租户id从商户表查询,如果存在记录则不允许添加商户
        Integer count1 = merchantMapper.selectCount(new LambdaQueryWrapper<Merchant>().eq(Merchant::getTenantId, tenantId));
        if (count1>0){
            throw new BusinessException(CommonErrorCode.E_200017);
        }

        //Merchant merchant = new Merchant();
        //merchant.setMobile(merchantDTO.getMobile());
        //写入其他属性
        //使用MapStruct进行对象转换
        Merchant merchant = MerchantConvert.INSTANCE.dto2entity(merchantDTO);
        //商户所对应的租户的id
        merchant.setTenantId(tenantId);
        //审核状态为0 未进行资质申请
        merchant.setAuditStatus("0");
        //调用数据库向mapper写入记录
        merchantMapper.insert(merchant);

        //新增门店
        StoreDTO storeDTO = new StoreDTO();
        storeDTO.setStoreName("根门店");
        storeDTO.setMerchantId(merchant.getId());
        storeDTO.setStoreStatus(true);
        StoreDTO store = createStore(storeDTO);
        //新增员工
        StaffDTO staffDTO = new StaffDTO();
        staffDTO.setMobile(merchantDTO.getMobile());//手机号
        staffDTO.setUsername(merchantDTO.getUsername());    //账号
        staffDTO.setStoreId(store.getId()); //员所属门店id
        staffDTO.setMerchantId(merchant.getId());   //商户的id
        //staffDTO.setStaffStatus(true);  //员工的状态，启用
        StaffDTO staff = createStaff(staffDTO);

        //为门店设置管理员
        bindStaffToStore(store.getId(),staff.getId());

        //merchantDTO.setId(merchant.getId());
        //将entity转换成dto

        return MerchantConvert.INSTANCE.entity2dto(merchant);
    }

    /***
     *
     * @param merchantId 商户id
     * @param merchantDTO   资质申请的信息
     * @throws BusinessException
     */
    @Override
    public void applyMerchant(Long merchantId, MerchantDTO merchantDTO) throws BusinessException {
        if (merchantId == null || merchantDTO == null) {
            //抛出自定义异常 传入对象为空或者缺少必要的参数
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        //校验merchantId的合法性，查询商户表，如果查询不到记录，则认为非法
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            //抛出自定义异常 商户不存在
            throw new BusinessException(CommonErrorCode.E_200002);
        }
        //将dto转换成entity
        Merchant entity = MerchantConvert.INSTANCE.dto2entity(merchantDTO);
        //将必要的参数设置到实例类中
        entity.setId(merchant.getId());
        //进行资质申请不允许修改手机号，要使用数据库中原来的手机号
        entity.setMobile(merchant.getMobile());
        //审核状态1 为已申请待审核
        entity.setAuditStatus("1");
        entity.setTenantId(merchant.getTenantId());


        //调用mapper更新商户表
        merchantMapper.updateById(entity);

    }


    /***
     * 新增门店
     * @param storeDTO 门店信息
     * @return
     * @throws BusinessException
     */
    @Override
    public StoreDTO createStore(StoreDTO storeDTO) throws BusinessException {
        Store entity = StoreConvert.INSTANCE.dto2entity(storeDTO);
        log.info("新增门店:{}", JSON.toJSONString(entity));
        //新增门店
        storeMapper.insert(entity);


        return StoreConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public StaffDTO createStaff(StaffDTO staffDTO) throws BusinessException {

        //参数合法性的校验
        if (staffDTO == null || StringUtils.isBlank(staffDTO.getMobile())
                || StringUtils.isBlank(staffDTO.getUsername())
                || staffDTO.getStoreId() == null
        ) {
            throw new BusinessException(CommonErrorCode.E_300009);
        }


        //同一个商户下员工的账号唯一
        Boolean exitsStaffByUserName = isExitsStaffByUserName(staffDTO.getUsername(), staffDTO.getMerchantId());
        if (exitsStaffByUserName) {
            throw new BusinessException(CommonErrorCode.E_100114);
        }


        //同一个商户下员工的手机号唯一

        Boolean existStaffByMobile = isExistStaffByMobile(staffDTO.getMobile(), staffDTO.getMerchantId());
        if (existStaffByMobile) {
            throw new BusinessException(CommonErrorCode.E_100113);
        }
        Staff staff = StaffConvert.INSTANCE.dto2entity(staffDTO);


        staffMapper.insert(staff);
        return StaffConvert.INSTANCE.entity2dto(staff);
    }


    /***
     * 将员工设置为门店的管理员
     * @param storeId
     * @param staffId
     * @throws BusinessException
     */
    @Override
    public void bindStaffToStore(Long storeId, Long staffId) throws BusinessException {
        StoreStaff storeStaff = new StoreStaff();
        storeStaff.setStaffId(staffId);
        storeStaff.setStoreId(storeId);
        storeStaffMapper.insert(storeStaff);


    }

    /***
     * 员工手机号在同一个商户下是唯一校验
     * @param mobile
     * @param merchantId
     * @return
     */
    Boolean isExistStaffByMobile(String mobile, Long merchantId) {
        Integer count = staffMapper.selectCount(new LambdaQueryWrapper<Staff>().eq(Staff::getMobile, mobile)
                .eq(Staff::getMerchantId, merchantId));
        return count > 0;
    }

    /***
     * 员工账号在同一个商户下是唯一校验
     * @param username
     * @param merchantId
     * @return
     */
    Boolean isExitsStaffByUserName(String username, Long merchantId) {
        Integer count = staffMapper.selectCount(new LambdaQueryWrapper<Staff>().eq(Staff::getUsername, username)
                .eq(Staff::getMerchantId, merchantId));

        return count > 0;
    }


}
