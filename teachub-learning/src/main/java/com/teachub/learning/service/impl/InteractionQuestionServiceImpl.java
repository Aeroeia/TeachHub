package com.teachub.learning.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teachub.common.exceptions.BadRequestException;
import com.teachub.common.exceptions.BizIllegalException;
import com.teachub.common.utils.BeanUtils;
import com.teachub.common.utils.UserContext;
import com.teachub.learning.domain.dto.QuestionFormDTO;
import com.teachub.learning.domain.po.InteractionQuestion;
import com.teachub.learning.mapper.InteractionQuestionMapper;
import com.teachub.learning.service.IInteractionQuestionService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 互动提问的问题表 服务实现类
 * </p>
 *
 * @author Aeroeia
 * @since 2025-07-19
 */
@Service
public class InteractionQuestionServiceImpl extends ServiceImpl<InteractionQuestionMapper, InteractionQuestion> implements IInteractionQuestionService {

    @Override
    public void addQuestion(QuestionFormDTO questionFormDTO) {
        Long userId = UserContext.getUser();
        if(userId==null){
            throw new BadRequestException("用户未登录");
        }
        InteractionQuestion interactionQuestion = BeanUtils.copyBean(questionFormDTO, InteractionQuestion.class);
        interactionQuestion.setUserId(userId);
        this.save(interactionQuestion);
    }

    @Override
    public void updateQuestion(QuestionFormDTO questionFormDTO,Long id) {
        Long userId = UserContext.getUser();
        if(userId==null){
            throw new BadRequestException("用户未登录");
        }
        InteractionQuestion interactionQuestion = this.lambdaQuery().eq(InteractionQuestion::getId, id).one();
        if(interactionQuestion==null){
            throw new BadRequestException("问题不存在");
        }
        if(!interactionQuestion.getUserId().equals(userId)){
            throw new BizIllegalException("不能修改他人问题");
        }
        String title = questionFormDTO.getTitle();
        String description = questionFormDTO.getDescription();
        Boolean anonymity = questionFormDTO.getAnonymity();
        this.lambdaUpdate().eq(InteractionQuestion::getId, id)
                .set(StrUtil.isNotBlank(title), InteractionQuestion::getTitle, title)
                .set(StrUtil.isNotBlank(description), InteractionQuestion::getDescription, description)
                .set(anonymity!=null, InteractionQuestion::getAnonymity, anonymity)
                .update();
    }
}
