package com.teachub.message.service.dubbo;

import com.teachub.message.api.client.MessageClient;
import com.teachub.message.domain.dto.SmsInfoDTO;
import com.teachub.message.service.ISmsService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

@DubboService
public class MessageDubboServiceImpl implements MessageClient {

    @Autowired
    private ISmsService smsService;

    @Override
    public void sendMessage(SmsInfoDTO smsInfoDTO) {
        // convert com.teachub.message.domain.dto.SmsInfoDTO to com.teachub.api.dto.sms.SmsInfoDTO
        com.teachub.api.dto.sms.SmsInfoDTO apiSmsInfoDTO = new com.teachub.api.dto.sms.SmsInfoDTO();
        apiSmsInfoDTO.setTemplateCode(smsInfoDTO.getTemplateCode());
        apiSmsInfoDTO.setPhones(smsInfoDTO.getPhones());
        apiSmsInfoDTO.setTemplateParams(smsInfoDTO.getTemplateParams());
        smsService.sendMessageAsync(apiSmsInfoDTO);
    }
}
