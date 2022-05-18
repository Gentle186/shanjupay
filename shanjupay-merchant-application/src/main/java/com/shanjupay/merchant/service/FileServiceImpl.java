package com.shanjupay.merchant.service;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.QiniuUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class FileServiceImpl implements FileService {

    @Value("${oss.qiniu.url}")
    private String qiniu_url;
    @Value("${oss.qiniu.accesskey}")
    private String accesskey;
    @Value("${oss.qiniu.secretkey}")
    private String secretkey;
    @Value("${oss.qiniu.bucket}")
    private String bucket;

    /***
     *
     * @param bytes  文件字节的数组
     * @param fileName 文件名
     * @return 返回值是文件的放温暖路径（绝对的url）
     * @throws BusinessException
     */
    @Override
    public String upload(byte[] bytes, String fileName) throws BusinessException {
        //调用common下的工具类


        try {
            QiniuUtils.upload2qiniu(accesskey,secretkey,bucket,bytes,fileName);
        } catch (RuntimeException e) {
            e.printStackTrace();
            //抛出自定义异常，上传错误
            throw new BusinessException(CommonErrorCode.E_100106);
        }
        //上传成功则返回文件的访问地址+文件名
        return qiniu_url+fileName;
    }
}
