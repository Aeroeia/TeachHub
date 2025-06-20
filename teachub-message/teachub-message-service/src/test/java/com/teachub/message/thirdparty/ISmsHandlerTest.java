package com.teachub.message.thirdparty;

import com.teachub.api.dto.sms.SmsInfoDTO;
import com.teachub.message.domain.enums.SmsTemplate;
import com.teachub.message.service.ISmsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;

@SpringBootTest
class ISmsHandlerTest {

    @Autowired
    private ISmsService smsService;

    @Test
    void send() {
        SmsInfoDTO dto = new SmsInfoDTO();
        dto.setPhones(List.of("13901517624", "15162153483"));
        dto.setTemplateCode(SmsTemplate.VERIFY_CODE.name());
        HashMap<String, String> params = new HashMap<>(1);
        params.put("code", "518518");
        dto.setTemplateParams(params);
        smsService.sendMessage(dto);
    }
}