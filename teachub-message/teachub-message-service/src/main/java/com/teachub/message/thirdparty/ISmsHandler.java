package com.teachub.message.thirdparty;

import com.teachub.api.dto.sms.SmsInfoDTO;
import com.teachub.message.domain.po.MessageTemplate;

/**
 * 第三方接口对接平台
 */
public interface ISmsHandler {

    /**
     * 发送短信
     */
    void send(SmsInfoDTO platformSmsInfoDTO, MessageTemplate template);


}
