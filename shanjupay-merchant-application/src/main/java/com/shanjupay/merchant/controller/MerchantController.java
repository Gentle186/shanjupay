package com.shanjupay.merchant.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.PhoneUtil;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;

import com.shanjupay.merchant.common.util.SecurityUtil;
import com.shanjupay.merchant.convert.MerchantDetailConvert;
import com.shanjupay.merchant.convert.MerchantRegisterConvert;
import com.shanjupay.merchant.service.FileService;
import com.shanjupay.merchant.service.SmsService;
import com.shanjupay.merchant.vo.MerchantDetailVO;
import com.shanjupay.merchant.vo.MerchantRegisterVO;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@Api(value = "商户平台应用接口", tags = "商户平台应用接口")
public class MerchantController {

    @Reference
    MerchantService merchantService;

    @Autowired //注入本地的bean
    SmsService smsService;

    @Autowired
    FileService fileService;


    @ApiOperation(value = "根据id查询商户信息")
    @GetMapping("/merchants/{id}")
    public MerchantDTO queryMerchantById(@PathVariable("id") Long id) {

        MerchantDTO merchantDTO = merchantService.queryMerchantById(id);
        return merchantDTO;
    }

    @ApiOperation("获取登录用户的商户信息")
    @GetMapping("/my/merchants")
    MerchantDTO getMyMerchantInfo() {
        //从token中获取商户的信息
        Long merchantId = SecurityUtil.getMerchantId();
        return merchantService.queryMerchantById(merchantId);
    }


    @ApiOperation("获取手机验证码")
    @GetMapping("/sms")
    @ApiImplicitParam(value = "手机号", name = "phone", required = true, dataType = "string", paramType = "query")
    public String getSMSCode(@RequestParam("phone") String phone) {
        //同验证码服务请求发送验证码
        return smsService.sendMsg(phone);
    }


    @ApiOperation("商户注册")
    @ApiImplicitParam(value = "商户注册信息", name = "merchantRegisterVO", required = true, dataType = "MerchantRegisterVO", paramType = "body")
    @PostMapping("/merchants/register")
    public MerchantRegisterVO registerMerchant(@RequestBody MerchantRegisterVO merchantRegisterVO) {

        //校验参数的合法性
        if (merchantRegisterVO == null) {
            throw new BusinessException(CommonErrorCode.E_100108);
        }
        if (StringUtils.isBlank(merchantRegisterVO.getMobile())) {
            throw new BusinessException(CommonErrorCode.E_100112);
        }
        if (!PhoneUtil.isMatches(merchantRegisterVO.getMobile())) {
            throw new BusinessException(CommonErrorCode.E_100109);
        }


        //校验验证码
        smsService.checkVerifiyCode(merchantRegisterVO.getVerifiykey(), merchantRegisterVO.getVerifiyCode());
        //调用dubbo服务接口
        //  MerchantDTO merchantDTO = new MerchantDTO();
        //向dto写入商户注册的信息
//        merchantDTO.setMobile(merchantRegisterVO.getMobile());
//        merchantDTO.setUsername(merchantRegisterVO.getUsername());

        //使用mapStruct来进行转换对象
        MerchantDTO merchantDTO = MerchantRegisterConvert.INSTANCE.vo2dto(merchantRegisterVO);
        merchantService.createMerchant(merchantDTO);
        return merchantRegisterVO;


    }

    @ApiOperation("上传证件照")
    @PostMapping("/upload")
    //上传证件照的一个接口    使用spring mvc来上传文件
    public String upload(@ApiParam(value = "上传证件照", required = true) @RequestParam("file") MultipartFile multipartFile) throws IOException {
        //调用fileService上传文件
        //生成的文件名称fileName,要保证它的唯一
        //显示文件的原始名称
        String originalFilename = multipartFile.getOriginalFilename();
        //获得文件的扩展名
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".") - 1);
        //通过扩展名来获取文件的名称
        String fileName = UUID.randomUUID() + suffix;
        return fileService.upload(multipartFile.getBytes(), fileName);
    }


    @ApiOperation("资质申请")
    @PostMapping("/my/merchant/save")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "merchantInfo", value = "商户认证资料", required = true,
                    dataType = "MerchantDetailVO", paramType = "body")
    })
    public void saveMerchant(MerchantDetailVO merchantInfo) {
        //解析token取出当前商户的id
        Long merchantId = SecurityUtil.getMerchantId();

        //vo转换dto
        MerchantDTO merchantDTO = MerchantDetailConvert.INSTANCE.vo2dto(merchantInfo);
        merchantService.applyMerchant(merchantId, merchantDTO);


    }


    @ApiOperation("测试")
    @GetMapping(path = "/hello")
    public String hello() {
        return "hello";
    }

    @ApiOperation("测试")
    @ApiImplicitParam(name = "name", value = "姓名", required = true, dataType = "string")
    @PostMapping(value = "/hi")
    public String hi(String name) {
        return "hi," + name;
    }
}
