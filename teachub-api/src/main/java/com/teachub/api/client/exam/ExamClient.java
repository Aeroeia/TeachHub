package com.teachub.api.client.exam;

import com.teachub.api.dto.exam.QuestionBizDTO;
import com.teachub.api.dto.exam.QuestionDTO;
import java.util.List;
import java.util.Map;

public interface ExamClient {

    void saveQuestionBizInfoBatch(Iterable<QuestionBizDTO> qbs);

    List<QuestionBizDTO> queryQuestionIdsByBizIds(Iterable<Long> bizIds);

    Map<Long, Integer> queryQuestionScoresByBizIds(Iterable<Long> bizIds);

    List<QuestionDTO> queryQuestionByIds(Iterable<Long> ids);

    Map<Long, Integer> countSubjectNumOfTeacher(Iterable<Long> createrIds);

    Map<Long, Integer> queryQuestionScores(Iterable<Long> ids);
}
