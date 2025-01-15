package com.teachub.message.service;

import com.teachub.message.domain.dto.UserInboxDTO;
import com.teachub.message.domain.dto.UserInboxFormDTO;
import com.teachub.api.dto.user.UserDTO;
import com.teachub.message.domain.query.UserInboxQuery;
import com.teachub.common.domain.dto.PageDTO;
import com.teachub.message.domain.po.NoticeTemplate;
import com.teachub.message.domain.po.UserInbox;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 用户通知记录 服务类
 * </p>
 *
 *
 * @  08-19
 */
public interface IUserInboxService extends IService<UserInbox> {

    void saveNoticeToInbox(NoticeTemplate noticeTemplate, List<UserDTO> users);

    PageDTO<UserInboxDTO> queryUserInBoxesPage(UserInboxQuery query);

    Long sentMessageToUser(UserInboxFormDTO userInboxFormDTO);
}
