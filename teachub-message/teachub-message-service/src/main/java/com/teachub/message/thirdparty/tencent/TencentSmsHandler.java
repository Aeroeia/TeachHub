package com.teachub.message.thirdparty.tencent;

import com.teachub.api.dto.sms.SmsInfoDTO;
import com.teachub.message.domain.po.MessageTemplate;
import com.teachub.message.thirdparty.ISmsHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service("tencent")
@Slf4j
public class TencentSmsHandler implements ISmsHandler {
    @Override
    public void send(SmsInfoDTO platformSmsInfoDTO, MessageTemplate template) {
        //第三方发送短信验证码
        log.info("tencent平台，短信发送成功 ...");
        log.info("platformSmsInfoDTO：{}", platformSmsInfoDTO);
    }
}
