package com.teachub.learning.service;

import com.teachub.common.domain.dto.PageDTO;
import com.teachub.learning.domain.dto.QuestionFormDTO;
import com.teachub.learning.domain.dto.QuestionPageQuery;
import com.teachub.learning.domain.po.InteractionQuestion;
import com.baomidou.mybatisplus.extension.service.IService;
import com.teachub.learning.domain.vo.QuestionVO;

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

    void updateQuestion(QuestionFormDTO questionFormDTO, Long id);

    PageDTO<QuestionVO> queryQuestions(QuestionPageQuery pageQuery);

    QuestionVO queryById(Long id);
}
