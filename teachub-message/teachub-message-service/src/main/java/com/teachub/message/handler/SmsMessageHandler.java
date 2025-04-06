package com.teachub.message.handler;

import com.teachub.api.dto.sms.SmsInfoDTO;
import com.teachub.common.constants.MqConstants;
import com.teachub.message.service.ISmsService;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = MqConstants.Topic.SMS_TOPIC, consumerGroup = "sms_consumer_group", selectorExpression = MqConstants.Tag.SMS_MESSAGE)
public class SmsMessageHandler implements RocketMQListener<SmsInfoDTO> {

    private final ISmsService smsService;

    @Override
    public void onMessage(SmsInfoDTO smsInfoDTO){
        smsService.sendMessage(smsInfoDTO);
    }
}
