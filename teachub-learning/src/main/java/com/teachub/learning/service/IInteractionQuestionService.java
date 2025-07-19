package com.teachub.learning.service;

import com.teachub.learning.domain.dto.QuestionFormDTO;
import com.teachub.learning.domain.po.InteractionQuestion;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 互动提问的问题表 服务类
 * </p>
 *
 * @author Aeroeia
 * @since 2025-07-20
 */
public interface IInteractionQuestionService extends IService<InteractionQuestion> {

    void addQuestion(QuestionFormDTO questionFormDTO);

}
