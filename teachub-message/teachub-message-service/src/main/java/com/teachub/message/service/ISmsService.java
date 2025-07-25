package com.teachub.message.service;

import com.teachub.api.dto.sms.SmsInfoDTO;
import com.teachub.api.dto.user.UserDTO;
import com.teachub.message.domain.po.NoticeTemplate;

import java.util.List;

public interface ISmsService {
    void sendMessageByTemplate(NoticeTemplate noticeTemplate, List<UserDTO> users);

    void sendMessage(SmsInfoDTO smsInfoDTO);

    void sendMessageAsync(SmsInfoDTO smsInfoDTO);
}
