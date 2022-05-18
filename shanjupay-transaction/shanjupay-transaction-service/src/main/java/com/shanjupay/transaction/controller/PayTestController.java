package com.shanjupay.transaction.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.alipay.api.AlipayConstants.APP_ID;

/**
 * 支付宝接口对接测试类
 * @author FreeLoop
 * @date 2022/5/17 14:36
 */



@Controller
//@RestController //请求方法响应统一json格式
public class PayTestController{

    String APP_ID = "2021000119697320";
    String APP_PRIVATE_KEY ="MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDEcRtYjzxUI3WBhv/1tKO5iEsTbsTS4jUAtmqtLw0Ji70DqC37I6/PyFnZQiKpb0joIgsH+5Aj6evccPgcqixc0VXcd1hF/lq8Kgzhw+sxgTtCfcGNBeqdxJcbnsOn/ud+kEYn7OlgiCOUB6ff12CMMx3YCWN3stOWMt/njWys2u+vK+5wnx0q7BsxNWDnJRSw28hU7UxwmQIq2iam6ghDDy087ULd0OYqGJRiDJEqTBAIV8pp2Dw+YzS4ykcxV6DIfF/WxxzEs8hIpz5PFD5poUwdga6JV8S0kMcg+jiRJ/gH8wLOnGtJjf8ZOL0SmEFdgXhHNZwrnH9W1SlxqnirAgMBAAECggEBAIs4mifeg+QhqVLeSaNqnaqNJKAmHgpTVTsnDV2l/fho99GSrNvTQ/Pfy4XFcGlivGoATo8ew94gGBAIZF90X7J8jfyM/JvGk6nNp4mN368bJtON6tG2grvDq1Bg6cuzRX+WYpJDPncypPxxnJFwxDU4uQr2RG995qUJc1RZD89Ernf4ZeFR5kYxCJD04kUfFAQ0hYQXTmE1y0i4711k8UBqTmswP5h3z4lsKijHHRfMwSywNzF+JL4j7HHtfQRH2mQMVfIr9wS58d/CS/ZK+o51DZCAjU0vD5j8OOlmfCfQxX06PhbScfNbQXp1E4032vdkiAobP3KLqDCsCio/nIkCgYEA///gSPy3QkfQyyMz7HkP4wTwSqSaeqQjy8sAmo7Id4Kg/EacHdCdK1kIeTIO3eaxP1zEqh7iTgH9otGQ/2y7lmHtsnxA4OwKEy6q8VK75Nqr4jyMn/LaPGaeZQFHbxv3iSfGsF9QSpT6xpCcKNAa4GlARoFGPAgpI0uCKqHNto8CgYEAxHEzrrPvktaWiF4MjGwXVlmfUkaLdGlq/Ez82VPLCjRhsn6T+YtKQBfAZ4eejwfqEUDd6hv7okXOl2Nc+ssgAhFJuIiyLVuewKDFr+7rRmLwblFtcZb3xtbpmpIfrX7XK4z/iFPQNHMAcj9vkLx0e11XvBcaijAktarhQ2AaiiUCgYB2G3tCsTVrYu61ibw+8Wue9XxXM7IBuxC4ySOyKwXZ/W6AdaM5LjIU+bKrF6k8P16vOQGS60TFcK2wHLek4Gl7bBraKfFU1BGh3/oF9IGziB+c9Qp8EPMWwtF5ECoA3QpLQQqt/ItynWFFkxo4yipcNrLs1F+u+qTgH8oiGRxriQKBgHq6wvBmSMpzuQ6vu6VLrvTq/2gW3O+5IPdKXjqlaFgSy+sJvAePZ69NZ48HIx/3cNygq0SlgPwsIttgX+nsWC4SgP2CoddGw5NTns7BNUr/a3lw9DCggmM/SCmjG5l4gpQZ/lg9DPqH0wKN8XT62MduVO8VfCZT18uMXDqM42flAoGBAIoq09c5SK7HjlA69zTVQVFd0BeF8ztGkf9dUNmJL1Lc2nqe1vDwT3mZg9/fGsl8eMsP3HGGmYQyntsV8ZJhyom3MqH2WOiu6ebhaSAQWuVQSVCLRMQRLH8ZC6IaoCZyrmtbhyiOvrEIj+GZihR+YZ9H6lLN4hFMJnxGsU25WZUY";


    String ALIPAY_PUBLIC_KEY ="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmCzZS60YVigUhBGrDOGHRVHT1qM5XbvcGA0O/jYLDCy9i4LrPGiInoowfpiW2A/ZPTdZvIoS6bJ0Gk0G+B+Qp9Ah+76f/6ZsPNt1Wqs+PNczEby2UmJuJcgxg7PKnG2+3lgoCU6d4OPHZ+vWnqhS98//eU7AT2hON621X5Hra73WCZ5DbQd+2abysmvoATtINSxlbyPOBrqMhO0JkGyDkfdWsABmfy1zs0JC4NBaZOh3r2A2W5EUjNeLFeKXwkpx2Q9QDb16rzunyVcp3Gq7UoUbrQWIXRcigfhKldcvgVBr6qJTlr8SURg4IjqxdGpWbNvHN1XPjQ1l+244RDpXxwIDAQAB";
    String CHARSET="utf-8";
    String servelUrl="https://openapi.alipaydev.com/gateway.do";
    @GetMapping("/alipaytest")
    public void alipaytest(HttpServletRequest httpRequest,
                       HttpServletResponse httpResponse) throws ServletException, IOException {
        AlipayClient alipayClient = new DefaultAlipayClient(servelUrl,APP_ID,APP_PRIVATE_KEY, "json", CHARSET, ALIPAY_PUBLIC_KEY, "RSA2"); //获得初始化的AlipayClient
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的request
        alipayRequest.setReturnUrl("http://domain.com/CallBack/return_url.jsp");
        alipayRequest.setNotifyUrl("http://domain.com/CallBack/notify_url.jsp");//在公共参数中设置回跳和通知地址
        alipayRequest.setBizContent("{" +
                " \"out_trade_no\":\"20150320010101002\"," +
                " \"total_amount\":\"88.88\"," +
                " \"subject\":\"Iphone6 16G\"," +
                " \"product_code\":\"QUICK_WAP_WAY\"" +
                " }");//填充业务参数
        String form="";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        httpResponse.setContentType("text/html;charset=" + CHARSET);
        httpResponse.getWriter().write(form);//直接将完整的表单html输出到页面
        httpResponse.getWriter().flush();
        httpResponse.getWriter().close();
    }





}