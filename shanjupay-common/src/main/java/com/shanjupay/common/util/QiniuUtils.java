package com.shanjupay.common.util;

/*七牛测试工具类包
 *
 *
 * */


import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;

import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.qiniu.util.IOUtils;
import com.shanjupay.common.domain.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

public class QiniuUtils {
    private static final Logger LOGGER= LoggerFactory.getLogger(QiniuUtils.class);


    //提供上传工具的方法,accesskey，secrekey,bucket,fileName


    /***
     *  文件上传工具的方法
     * @param accessKey
     * @param secretKey
     * @param bucket
     * @param bytes
     * @param fileName  外部传值，七牛云上面的文件名和此文件名保持一致
     */


    public static void upload2qiniu(String accessKey, String secretKey, String bucket, byte[] bytes, String fileName) throws RuntimeException{

        //构造一个带指定 Region 对象的配置类
        Configuration cfg = new Configuration(Region.huanan());
        //...其他参数参考类注释
        UploadManager uploadManager = new UploadManager(cfg);
        //...生成上传凭证，然后准备上传
        //默认不指定key的情况下，以文件内容的hash值作为文件名，这里建议由自己来控制文件名
        String key = fileName;
        FileInputStream fileInputStream = null;
        try {
            Auth auth = Auth.create(accessKey, secretKey);
            String upToken = auth.uploadToken(bucket);
            try {
                //上传文件，参数:字节数组,key,token令牌
                //key:建议我们自己生成一个不重复的名称
                Response response = uploadManager.put(bytes, key, upToken);
                //解析上传成功的结果
                DefaultPutRet putRet = new Gson().fromJson(response.bodyString(),
                        DefaultPutRet.class);
                System.out.println(putRet.key);
                System.out.println(putRet.hash);
            } catch (QiniuException ex) {
                Response r = ex.response;
                System.err.println(r.toString());
                LOGGER.error("上传文件到七牛:{}",ex.getMessage());
                try {
                    LOGGER.error(r.bodyString());
                } catch (QiniuException ex2) {
                //ignore
                }
            }
        } catch (Exception ex) {
            LOGGER.error("上传到七牛:{}",ex.getMessage());
            //ignore
            throw new RuntimeException(ex.getMessage());
        }

       /* finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
*/



    }

    //上传测试
    private static void testUpload() {
//构造一个带指定 Region 对象的配置类
        Configuration cfg = new Configuration(Region.huanan());
//...其他参数参考类注释
        UploadManager uploadManager = new UploadManager(cfg);
//...生成上传凭证，然后准备上传
        String accessKey = "OTQI6t8CmZVNyP4xuOZdj9omNC3TWCVycotT8ItR";
        String secretKey = "pjT0GcqccdCyt-iYto0fdCuZVEdLwVcw5nvOl3v2";
        String bucket = "shanjupay-b1";
//默认不指定key的情况下，以文件内容的hash值作为文件名，这里建议由自己来控制文件名
        String key = UUID.randomUUID().toString() + "B.png";
        FileInputStream fileInputStream = null;
        try {
//通常这里得到文件的字节数组
            String path = "F:\\1.png";
            fileInputStream = new FileInputStream(new File(path));
            //得到本地文件的字节数组
            byte[] bytes = IOUtils.toByteArray(fileInputStream);
            Auth auth = Auth.create(accessKey, secretKey);
            String upToken = auth.uploadToken(bucket);
            try {
                Response response = uploadManager.put(bytes, key, upToken);
                //解析上传成功的结果
                DefaultPutRet putRet = new Gson().fromJson(response.bodyString(),
                        DefaultPutRet.class);
                System.out.println(putRet.key);
                System.out.println(putRet.hash);
            } catch (QiniuException ex) {
                Response r = ex.response;
                System.err.println(r.toString());
                try {
                    System.err.println(r.bodyString());
                } catch (QiniuException ex2) {
//ignore
                }
            }
        } catch (IOException ex) {
//ignore
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        QiniuUtils.testGetFileUrl();
    }

    //获取文件url
    private static void testGetFileUrl() throws UnsupportedEncodingException {

        String fileName = "06407489-4b08-4735-bf22-08194a7835e7B.png";
        String domainOfBucket = "http://rbtm7uvsn.hn-bkt.clouddn.com";
        String encodedFileName = URLEncoder.encode(fileName, "utf-8").replace("+", "%20");
        String publicUrl = String.format("%s/%s", domainOfBucket, encodedFileName);
        String accessKey = "OTQI6t8CmZVNyP4xuOZdj9omNC3TWCVycotT8ItR";
        String secretKey = "pjT0GcqccdCyt-iYto0fdCuZVEdLwVcw5nvOl3v2";
        Auth auth = Auth.create(accessKey, secretKey);
        long expireInSeconds = 3600;//1小时，可以自定义链接过期时间
        String finalUrl = auth.privateDownloadUrl(publicUrl, expireInSeconds);
        System.out.println(finalUrl);
    }

}