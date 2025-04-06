package com.teachub.message.api.client;

import com.teachub.common.autoconfigure.mq.RocketMqHelper;
import com.teachub.common.constants.MqConstants;
import com.teachub.message.domain.dto.SmsInfoDTO;

public class AsyncSmsClient {
    private final RocketMqHelper mqHelper;

    public AsyncSmsClient(RocketMqHelper mqHelper) {
        this.mqHelper = mqHelper;
    }

    /**
     * 基于 MQ 异步发送短信
     * @param smsInfoDTO 短信相关信息
     */
    public void sendMessage(SmsInfoDTO smsInfoDTO){
        mqHelper.send(MqConstants.Topic.SMS_TOPIC, MqConstants.Tag.SMS_MESSAGE, smsInfoDTO);
    }
}
