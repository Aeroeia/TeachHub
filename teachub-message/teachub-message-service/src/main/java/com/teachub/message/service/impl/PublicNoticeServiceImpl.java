package com.teachub.message.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teachub.message.config.MessageProperties;
import com.teachub.message.domain.po.NoticeTemplate;
import com.teachub.message.domain.po.PublicNotice;
import com.teachub.message.mapper.PublicNoticeMapper;
import com.teachub.message.service.IPublicNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 公告消息模板 服务实现类
 * </p>
 *
 *   
 * @  08-19
 */
@Service
@RequiredArgsConstructor
public class PublicNoticeServiceImpl extends ServiceImpl<PublicNoticeMapper, PublicNotice> implements IPublicNoticeService {

    private final MessageProperties messageProperties;
    @Override
    public void saveNoticeOfTemplate(NoticeTemplate noticeTemplate) {
        LocalDateTime now = LocalDateTime.now();
        PublicNotice notice = new PublicNotice();
        notice.setTitle(noticeTemplate.getTitle());
        notice.setContent(noticeTemplate.getContent());
        notice.setPushTime(now);
        notice.setType(noticeTemplate.getType());
        notice.setExpireTime(now.plusMonths(messageProperties.getNoticeTtlMonths()));
        save(notice);
    }
}
