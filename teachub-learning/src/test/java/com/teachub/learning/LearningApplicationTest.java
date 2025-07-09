package com.teachub.learning;

import com.teachub.api.dto.trade.OrderBasicDTO;
import com.teachub.common.autoconfigure.mq.RabbitMqHelper;
import com.teachub.common.constants.MqConstants;
import com.teachub.common.utils.CollUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LearningApplicationTest {
    @Autowired
    private  RabbitMqHelper rabbitMqHelper;
    @Test
    void refundMqTest(){
        rabbitMqHelper.send(
                MqConstants.Exchange.ORDER_EXCHANGE,
                MqConstants.Key.ORDER_REFUND_KEY,
                OrderBasicDTO.builder()
                        .userId(129L)
                        .courseIds(CollUtils.singletonList(1549025085494521857L)).build());
    }
}