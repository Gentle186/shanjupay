package com.shanjupay.transaction.controller;

import com.alibaba.fastjson.JSON;
import com.github.wxpay.sdk.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author FreeLoop
 * @date 2022/5/17 16:37
 */

@Slf4j
@Controller
public class WxPayController {
    //微信测试参数
    String appID = "wxd2bf2dba2e86a8c7";
    String mchID = "1502570431";
    String appSecret = "cec1a9185ad435abe1bced4b93f7ef2e";
    String key = "95fe355daca50f1ae82f0865c2ce87c8";
    //申请授权码地址
    String wxOAuth2RequestUrl = "https://open.weixin.qq.com/connect/oauth2/authorize";
    //授权回调地址
    String wxOAuth2CodeReturnUrl = "http://xfc.nat300.top/transaction/wx‐oauth‐code‐return";
    String state = "";

    //获取授权码
    @GetMapping("/getWXOAuthCode")
    public String getWxOAuth2Code(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
// https://open.weixin.qq.com/connect/oauth2/authorize?
        // appid=APPID&redirect_uri=REDIRECT_URI&response_type=code&scope=SCOPE&state=STATE#wechat_redirect
        String url = String
                .format("%s?appid=%s&scope=snsapi_base&state=%s&redirect_uri=%s",
                        wxOAuth2RequestUrl, appID,
                        state, URLEncoder.encode(wxOAuth2CodeReturnUrl, "utf-8"));
        return "redirect:" + url;
    }


    /***
     * 授权回调 传入授权码和state
     * @param code 授权码
     * @param state 申请授权码传入微信的值被原样返回
     * @return
     */
    @GetMapping("/wx-oauth-code-return")
    public String wxOAuth2CodeReturn(@RequestParam String code, @RequestParam String state) {

        //https://api.weixin.qq.com/sns/oauth2/access_token?
        //appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
//申请openid
        String url = String
                .format("https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code", appID, appSecret, code, "utf-8");


        //申请openid,请求url
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
        //申请openid接口响应的内容,其中包括了openid
        String body = exchange.getBody();
        log.info("申请openid响应的内容{}", body);
        //获取openid
        String openid = JSON.parseObject(body).getString("openid");

        return "redirect:http://xfc.nat300.top/transaction/wxjspay?openid=" + openid;
    }

    //统一下单，接收openid
    @GetMapping("/wxjspay")
    public ModelAndView wxjspay(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //创建sdk客户端
        WXPay wxPay = new WXPay(new WXPayConfigCustom());

        //构造请求的参数
        Map<String, String> requestParam = new HashMap<>();
        requestParam.put("body", "iphone8");//订单描述
        requestParam.put("out_trade_no", "1234567");//订单号
        requestParam.put("fee_type", "CNY");//人民币
        requestParam.put("total_fee", String.valueOf(1)); //金额
        requestParam.put("spbill_create_ip", "127.0.0.1");//客户端ip
        requestParam.put("notify_url", "none");//微信异步通知支付结果接口，暂时不用
        requestParam.put("trade_type", "JSAPI");
        requestParam.put("openid", request.getParameter("openid"));
        //调用微信统一下单API
        //从请求中获取openid
        String openid = request.getParameter("openid");
        requestParam.put("openid",openid);
        //调用统一单接口
        Map<String, String> resp = wxPay.unifiedOrder(requestParam);

        //准备h5网页需要的数据
        //根据返回预付单信息生成JSAPI页面调用的支付参数并签名
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        Map<String, String> jsapiPayParam = new HashMap<>();
        jsapiPayParam.put("appId", resp.get("appid"));
        jsapiPayParam.put("package", "prepay_id=" + resp.get("prepay_id"));
        jsapiPayParam.put("timeStamp", timestamp);
        jsapiPayParam.put("nonceStr", UUID.randomUUID().toString());
        jsapiPayParam.put("signType", "HMAC‐SHA256");
        jsapiPayParam.put("paySign",
                WXPayUtil.generateSignature(jsapiPayParam, key,
                        WXPayConstants.SignType.HMACSHA256));
        log.info("微信JSAPI支付响应内容：" + jsapiPayParam);

        //将h5网页响应给前端
        return new ModelAndView("wxpay", jsapiPayParam);


    }
    class  WXPayConfigCustom extends WXPayConfig{
        @Override
        protected String getAppID() {
            return appID;
        }

        @Override
        protected String getMchID() {
            return mchID;
        }

        @Override
        protected String getKey() {
            return key;
        }

        @Override
        protected InputStream getCertStream() {
            return null;
        }

        @Override
        protected IWXPayDomain getWXPayDomain() {
            return new IWXPayDomain() {
                @Override
                public void report(String s, long l, Exception e) {

                }

                @Override
                public DomainInfo getDomain(WXPayConfig wxPayConfig) {
                    return new DomainInfo(WXPayConstants.DOMAIN_API, true);
                }
            };
        }
    }


}

