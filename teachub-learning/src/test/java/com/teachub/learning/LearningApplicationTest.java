package com.teachub.learning;

import com.teachub.api.dto.trade.OrderBasicDTO;
import com.teachub.common.autoconfigure.mq.RocketMqHelper;
import com.teachub.common.constants.MqConstants;
import com.teachub.common.utils.CollUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LearningApplicationTest {
    @Autowired
    private  RocketMqHelper rocketMqHelper;
    @Test
    void refundMqTest(){
        rocketMqHelper.send(
                MqConstants.Topic.ORDER_TOPIC,
                MqConstants.Tag.ORDER_REFUND,
                OrderBasicDTO.builder()
                        .userId(129L)
                        .courseIds(CollUtils.singletonList(1549025085494521857L)).build());
    }
}