package com.teachub.message.api.client;

import com.teachub.message.domain.dto.SmsInfoDTO;

public interface MessageClient {

    /**
     * 同步发送短信
     * @param smsInfoDTO 短信相关信息
     */
    void sendMessage(SmsInfoDTO smsInfoDTO);
}
