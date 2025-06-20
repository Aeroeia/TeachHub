package com.teachub.message.service;

import com.teachub.message.domain.dto.MessageTemplateDTO;
import com.teachub.message.domain.dto.MessageTemplateFormDTO;
import com.teachub.message.domain.query.MessageTemplatePageQuery;
import com.teachub.common.domain.dto.PageDTO;
import com.teachub.message.domain.po.MessageTemplate;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 第三方短信平台签名和模板信息 服务类
 * </p>
 *
 *
 * @  08-19
 */
public interface IMessageTemplateService extends IService<MessageTemplate> {

    List<MessageTemplate> queryByNoticeTemplateId(Long id);

    Long saveMessageTemplate(MessageTemplateFormDTO messageTemplateDTO);

    void updateMessageTemplate(MessageTemplateFormDTO messageTemplateDTO);

    PageDTO<MessageTemplateDTO> queryMessageTemplates(MessageTemplatePageQuery pageQuery);

    MessageTemplateDTO queryMessageTemplate(Long id);
}
