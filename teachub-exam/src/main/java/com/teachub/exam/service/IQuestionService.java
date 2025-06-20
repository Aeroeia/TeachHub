package com.teachub.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teachub.api.dto.exam.QuestionDTO;
import com.teachub.common.domain.dto.PageDTO;
import com.teachub.exam.domain.dto.QuestionFormDTO;
import com.teachub.exam.domain.po.Question;
import com.teachub.exam.domain.query.QuestionPageQuery;
import com.teachub.exam.domain.vo.QuestionDetailVO;
import com.teachub.exam.domain.vo.QuestionPageVO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 题目 服务类
 * </p>
 *
 *   
 * @  09-02
 */
public interface IQuestionService extends IService<Question> {

    void addQuestion(QuestionFormDTO questionFormDTO);

    void updateQuestion(QuestionFormDTO questionDTO);

    void deleteQuestionById(Long id);

    PageDTO<QuestionPageVO> queryQuestionByPage(QuestionPageQuery query);

    QuestionDetailVO queryQuestionDetailById(Long id);

    List<QuestionDTO> queryQuestionByIds(List<Long> ids);

    Map<Long, Integer> countQuestionNumOfCreater(List<Long> createrIds);

    List<QuestionDTO> queryQuestionByBizId(Long bizId);

    Boolean checkNameValid(String name);

    Map<Long, Integer> queryQuestionScores(List<Long> ids);
}
