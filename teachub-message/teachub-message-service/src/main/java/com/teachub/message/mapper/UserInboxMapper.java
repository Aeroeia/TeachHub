package com.teachub.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teachub.message.domain.po.UserInbox;

/**
 * <p>
 * 用户通知记录 Mapper 接口
 * </p>
 *
 *
 * @  08-20
 */
public interface UserInboxMapper extends BaseMapper<UserInbox> {

    UserInbox queryLatestPublicNotice(Long userId);
}
