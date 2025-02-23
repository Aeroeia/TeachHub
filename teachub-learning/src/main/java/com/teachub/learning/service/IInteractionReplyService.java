package com.teachub.learning.service;

import com.teachub.common.domain.dto.PageDTO;
import com.teachub.learning.domain.dto.ReplyDTO;
import com.teachub.learning.domain.dto.ReplyPageQuery;
import com.teachub.learning.domain.po.InteractionReply;
import com.baomidou.mybatisplus.extension.service.IService;
import com.teachub.learning.domain.vo.ReplyVO;

/**
 * <p>
 * 互动问题的回答或评论 服务类
 * </p>
 *
 * @author Aeroeia
 * @since 2025-07-20
 */
public interface IInteractionReplyService extends IService<InteractionReply> {

    void postReply(ReplyDTO replyDTO);

    PageDTO<ReplyVO> pageQuery(ReplyPageQuery replyPageQuery,boolean isAdmin);
}
