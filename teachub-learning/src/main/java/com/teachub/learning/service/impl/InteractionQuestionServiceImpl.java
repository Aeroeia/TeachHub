package com.teachub.learning.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teachub.api.client.user.UserClient;
import com.teachub.api.dto.user.UserDTO;
import com.teachub.common.domain.dto.PageDTO;
import com.teachub.common.exceptions.BadRequestException;
import com.teachub.common.exceptions.BizIllegalException;
import com.teachub.common.utils.BeanUtils;
import com.teachub.common.utils.CollUtils;
import com.teachub.common.utils.UserContext;
import com.teachub.learning.domain.dto.QuestionFormDTO;
import com.teachub.learning.domain.dto.QuestionPageQuery;
import com.teachub.learning.domain.po.InteractionQuestion;
import com.teachub.learning.domain.po.InteractionReply;
import com.teachub.learning.domain.vo.QuestionVO;
import com.teachub.learning.mapper.InteractionQuestionMapper;
import com.teachub.learning.service.IInteractionQuestionService;
import com.teachub.learning.service.IInteractionReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <p>
 * 互动提问的问题表 服务实现类
 * </p>
 *
 * @author Aeroeia
 * @since 2025-07-19
 */
@Service
@RequiredArgsConstructor
public class InteractionQuestionServiceImpl extends ServiceImpl<InteractionQuestionMapper, InteractionQuestion> implements IInteractionQuestionService {
    private final UserClient userClient;
    private final IInteractionReplyService replyService;

    @Override
    public void addQuestion(QuestionFormDTO questionFormDTO) {
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new BadRequestException("用户未登录");
        }
        InteractionQuestion interactionQuestion = BeanUtils.copyBean(questionFormDTO, InteractionQuestion.class);
        interactionQuestion.setUserId(userId);
        this.save(interactionQuestion);
    }

    @Override
    public void updateQuestion(QuestionFormDTO questionFormDTO, Long id) {
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new BadRequestException("用户未登录");
        }
        InteractionQuestion interactionQuestion = this.lambdaQuery().eq(InteractionQuestion::getId, id).one();
        if (interactionQuestion == null) {
            throw new BadRequestException("问题不存在");
        }
        if (!interactionQuestion.getUserId().equals(userId)) {
            throw new BizIllegalException("不能修改他人问题");
        }
        String title = questionFormDTO.getTitle();
        String description = questionFormDTO.getDescription();
        Boolean anonymity = questionFormDTO.getAnonymity();
        this.lambdaUpdate().eq(InteractionQuestion::getId, id)
                .set(StrUtil.isNotBlank(title), InteractionQuestion::getTitle, title)
                .set(StrUtil.isNotBlank(description), InteractionQuestion::getDescription, description)
                .set(anonymity != null, InteractionQuestion::getAnonymity, anonymity)
                .update();
    }

    @Override
    public PageDTO<QuestionVO> queryQuestions(QuestionPageQuery pageQuery) {
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new BadRequestException("用户未登录");
        }
        //构造分页参数
        Page<InteractionQuestion> page = pageQuery.toMpPageDefaultSortByCreateTimeDesc();
        Page<InteractionQuestion> p = this.lambdaQuery().eq(pageQuery.getOnlyMine() != null && pageQuery.getOnlyMine(), InteractionQuestion::getUserId, userId)
                .eq(InteractionQuestion::getCourseId, pageQuery.getCourseId())
                .eq(pageQuery.getSectionId() != null, InteractionQuestion::getSectionId, pageQuery.getSectionId())
                .eq(InteractionQuestion::getHidden, false)
                //排出description
                .select(InteractionQuestion.class, new Predicate<TableFieldInfo>() {
                    @Override
                    public boolean test(TableFieldInfo tableFieldInfo) {
                        return !tableFieldInfo.getProperty().equals("description");
                    }
                })
                .page(page);
        List<InteractionQuestion> records = p.getRecords();
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(p);
        }
        //根据最新回答用户id进行分类
        List<Long> answerIds = records.stream().map(InteractionQuestion::getLatestAnswerId).filter(Objects::nonNull).collect(Collectors.toList());
        List<InteractionReply> interactionReplies = replyService.listByIds(answerIds);
        Set<Long> collect = interactionReplies.stream().map(InteractionReply::getUserId).collect(Collectors.toSet());
        List<Long> userIds = records.stream().map(InteractionQuestion::getUserId).collect(Collectors.toList());
        collect.addAll(userIds);
        List<UserDTO> users = userClient.queryUserByIds(collect);
        Map<Long, UserDTO> userMap = users.stream().collect(Collectors.toMap(UserDTO::getId, u -> u));
        Map<Long, InteractionReply> interactionReplyMap = interactionReplies.stream().collect(Collectors.toMap(InteractionReply::getId, r -> r));
        List<QuestionVO> result = new ArrayList<>();
        //po->vo
        for (InteractionQuestion question : records) {
            QuestionVO questionVO = BeanUtils.toBean(question, QuestionVO.class);
            if(!questionVO.getAnonymity()){
                questionVO.setUserName(userMap.get(question.getUserId()).getName());
                questionVO.setUserIcon(userMap.get(question.getUserId()).getIcon());
            }
            if (question.getLatestAnswerId() != null) {
                questionVO.setLatestReplyContent(interactionReplyMap.get(question.getLatestAnswerId()).getContent());
                questionVO.setLatestReplyUser(userMap.get(interactionReplyMap.get(question.getLatestAnswerId()).getUserId()).getName());
            }

            result.add(questionVO);
        }
        return PageDTO.of(p, result);
    }

    @Override
    public QuestionVO queryById(Long id) {
        //非法校验
        if(id==null){
            throw new BadRequestException("问题id不能为空");
        }
        InteractionQuestion interactionQuestion = this.lambdaQuery().eq(InteractionQuestion::getId, id).one();
        if(interactionQuestion==null){
            throw new BadRequestException("问题不存在");
        }
        //判断是否隐藏
        if(interactionQuestion.getHidden()){
            return null;
        }
        QuestionVO questionVO = BeanUtils.copyBean(interactionQuestion, QuestionVO.class);
        if(!interactionQuestion.getAnonymity()){
            UserDTO userDTO = userClient.queryUserById(interactionQuestion.getUserId());
            if(userDTO==null){
                throw new BadRequestException("用户不存在");
            }
            questionVO.setUserName(userDTO.getName());
            questionVO.setUserIcon(userDTO.getIcon());
        }
        return questionVO;
    }

    @Override
    public void delete(Long id) {
        Long userId = UserContext.getUser();
        if(userId==null){
            throw new BadRequestException("用户未登录");
        }
        InteractionQuestion interactionQuestion = this.lambdaQuery().eq(InteractionQuestion::getId, id).one();
        if(interactionQuestion==null){
            throw new BadRequestException("问题不存在");
        }
        if(!userId.equals(interactionQuestion.getUserId())){
            throw new BizIllegalException("不能删除他人问题");
        }
        boolean remove = this.lambdaUpdate().eq(InteractionQuestion::getId, id).remove();
        if(!remove){
            throw new BizIllegalException("删除问题失败");
        }
    }
}
