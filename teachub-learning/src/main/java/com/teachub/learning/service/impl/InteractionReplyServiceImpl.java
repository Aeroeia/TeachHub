package com.teachub.learning.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teachub.api.client.user.UserClient;
import com.teachub.api.dto.user.UserDTO;
import com.teachub.common.domain.dto.PageDTO;
import com.teachub.common.exceptions.BadRequestException;
import com.teachub.common.utils.BeanUtils;
import com.teachub.common.utils.CollUtils;
import com.teachub.common.utils.UserContext;
import com.teachub.learning.domain.dto.ReplyDTO;
import com.teachub.learning.domain.dto.ReplyPageQuery;
import com.teachub.learning.domain.po.InteractionQuestion;
import com.teachub.learning.domain.po.InteractionReply;
import com.teachub.learning.domain.vo.ReplyVO;
import com.teachub.learning.mapper.InteractionReplyMapper;
import com.teachub.learning.service.IInteractionQuestionService;
import com.teachub.learning.service.IInteractionReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 互动问题的回答或评论 服务实现类
 * </p>
 *
 * @author Aeroeia
 * @since 2025-07-19
 */
@Service
@RequiredArgsConstructor
public class InteractionReplyServiceImpl extends ServiceImpl<InteractionReplyMapper, InteractionReply> implements IInteractionReplyService {
    @Autowired
    @Lazy
    private IInteractionQuestionService interactionQuestionService;
    private final UserClient userClient;

    @Override
    @Transactional
    public void postReply(ReplyDTO replyDTO) {
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new BadRequestException("用户未登录");
        }
        InteractionReply interactionReply = BeanUtils.copyBean(replyDTO, InteractionReply.class);
        interactionReply.setUserId(userId);
        if (replyDTO.getIsStudent() != null && replyDTO.getIsStudent()) {
            interactionQuestionService.lambdaUpdate().eq(InteractionQuestion::getId, replyDTO.getQuestionId())
                    .setSql("answer_times=answer_times+1")
                    .update();
        }
        this.save(interactionReply);
    }

    @Override
    public PageDTO<ReplyVO> pageQuery(ReplyPageQuery replyPageQuery, boolean isAdmin) {
        if (replyPageQuery.getQuestionId() != null) {
            return handleQuestion(replyPageQuery,isAdmin);
        }
        return handleAnswer(replyPageQuery,isAdmin);
    }

    @Override
    public void updateHidden(Long id, Boolean hidden) {
        this.lambdaUpdate().eq(InteractionReply::getId,id)
                .set(InteractionReply::getHidden,hidden)
                .update();
    }


    private PageDTO<ReplyVO> handleQuestion(ReplyPageQuery replyPageQuery, boolean isAdmin) {
        //构建分页条件
        Page<InteractionReply> page = this.lambdaQuery().eq(InteractionReply::getQuestionId, replyPageQuery.getQuestionId())
                .eq(!isAdmin,InteractionReply::getHidden, false)
                .eq(InteractionReply::getAnswerId, 0)
                .page(replyPageQuery.toMpPageDefaultSortByCreateTimeDesc());
        List<InteractionReply> records = page.getRecords();
        //判断非空
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(page);
        }
        //转vo
        List<ReplyVO> replyVOS = BeanUtils.copyList(records, ReplyVO.class);
        //获取用户信息
        Set<Long> userIds = records.stream().map(InteractionReply::getUserId).collect(Collectors.toSet());
        List<UserDTO> userDTOS = userClient.queryUserByIds(userIds);
        Map<Long, UserDTO> userMap = userDTOS.stream().collect(Collectors.toMap(UserDTO::getId, u -> u));
        //赋值
        for (ReplyVO replyVO : replyVOS) {
            replyVO.setUserType(userMap.get(replyVO.getUserId()).getType());
            if (!replyVO.getAnonymity()||isAdmin) {
                replyVO.setUserName(userMap.get(replyVO.getUserId()).getName());
                replyVO.setUserIcon(userMap.get(replyVO.getUserId()).getIcon());
            }
        }
        return PageDTO.of(page, replyVOS);

    }

    private PageDTO<ReplyVO> handleAnswer(ReplyPageQuery replyPageQuery,boolean isAdmin) {
        //分页条件构造
        Page<InteractionReply> page = this.lambdaQuery().eq(InteractionReply::getAnswerId, replyPageQuery.getAnswerId())
                .eq(!isAdmin,InteractionReply::getHidden, false)
                .page(replyPageQuery.toMpPageDefaultSortByCreateTimeDesc());
        List<InteractionReply> records = page.getRecords();
        //非空判断
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(page);
        }
        //获取用户信息
        Set<Long> ids = new HashSet<>();
        for(InteractionReply r : records){
            ids.add(r.getUserId());
            if(r.getTargetUserId()!=null){
                ids.add(r.getTargetUserId());
            }
        }
        List<UserDTO> userDTOS = userClient.queryUserByIds(ids);
        Map<Long, UserDTO> userMap = userDTOS.stream().collect(Collectors.toMap(UserDTO::getId, u -> u));
        //po->vo
        List<ReplyVO> result = new ArrayList<>();
        for(InteractionReply reply : records){
            ReplyVO replyVO = BeanUtils.copyBean(reply, ReplyVO.class);
            if(!replyVO.getAnonymity()||isAdmin){
                replyVO.setUserIcon(userMap.get(reply.getUserId()).getIcon());
                replyVO.setUserName(userMap.get(reply.getUserId()).getName());
            }
            replyVO.setUserType(userMap.get(reply.getUserId()).getType());
            if(reply.getTargetUserId() != null){
                replyVO.setTargetUserName(userMap.get(reply.getTargetUserId()).getName());
            }
            result.add(replyVO);
        }
        return PageDTO.of(page, result);

    }
}
