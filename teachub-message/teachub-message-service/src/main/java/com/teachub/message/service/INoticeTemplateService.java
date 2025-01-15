package com.teachub.message.service;

import com.teachub.message.domain.dto.NoticeTemplateDTO;
import com.teachub.message.domain.dto.NoticeTemplateFormDTO;
import com.teachub.message.domain.query.NoticeTemplatePageQuery;
import com.teachub.common.domain.dto.PageDTO;
import com.teachub.message.domain.po.NoticeTemplate;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 通知模板 服务类
 * </p>
 *
 *   
 * @  08-19
 */
public interface INoticeTemplateService extends IService<NoticeTemplate> {

    Long saveNoticeTemplate(NoticeTemplateFormDTO noticeTemplateFormDTO);

    void updateNoticeTemplate(NoticeTemplateFormDTO noticeTemplateFormDTO);

    PageDTO<NoticeTemplateDTO> queryNoticeTemplates(NoticeTemplatePageQuery pageQuery);

    NoticeTemplateDTO queryNoticeTemplate(Long id);

    NoticeTemplate queryByCode(String code);
}
