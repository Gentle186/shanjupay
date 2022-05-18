package com.shanjupay.transaction;

import com.shanjupay.transaction.api.PayChannelService;
import com.shanjupay.transaction.api.dto.PayChannelDTO;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @author FreeLoop
 * @date 2022/5/15 15:22
 */

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestPayChannelService {


    //测试根据服务类型查询支付的渠道

    @Autowired
    PayChannelService payChannelService;

    public void testqueryPayChannelByPlatformChannel(){

        List<PayChannelDTO> shanju_c2b = payChannelService.queryPayChannelByPlatformChannel("shanju_c2b" );
        System.out.println(shanju_c2b);

    }




}
