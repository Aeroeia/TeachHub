package com.teachub.exam.service.dubbo;

import com.teachub.api.client.exam.ExamClient;
import com.teachub.api.dto.exam.QuestionBizDTO;
import com.teachub.api.dto.exam.QuestionDTO;
import com.teachub.exam.service.IQuestionBizService;
import com.teachub.exam.service.IQuestionService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@DubboService
public class ExamDubboServiceImpl implements ExamClient {

    @Autowired
    private IQuestionBizService bizService;
    @Autowired
    private IQuestionService questionService;

    @Override
    public void saveQuestionBizInfoBatch(Iterable<QuestionBizDTO> qbs) {
        List<QuestionBizDTO> list = new ArrayList<>();
        if(qbs != null) qbs.forEach(list::add);
        bizService.saveQuestionBizInfoBatch(list);
    }

    @Override
    public List<QuestionBizDTO> queryQuestionIdsByBizIds(Iterable<Long> bizIds) {
        List<Long> list = new ArrayList<>();
        if(bizIds != null) bizIds.forEach(list::add);
        return bizService.queryQuestionIdsByBizIds(list);
    }

    @Override
    public Map<Long, Integer> queryQuestionScoresByBizIds(Iterable<Long> bizIds) {
        List<Long> list = new ArrayList<>();
        if(bizIds != null) bizIds.forEach(list::add);
        return bizService.queryQuestionScoresByBizIds(list);
    }

    @Override
    public List<QuestionDTO> queryQuestionByIds(Iterable<Long> ids) {
        List<Long> list = new ArrayList<>();
        if(ids != null) ids.forEach(list::add);
        return questionService.queryQuestionByIds(list);
    }

    @Override
    public Map<Long, Integer> countSubjectNumOfTeacher(Iterable<Long> createrIds) {
        List<Long> list = new ArrayList<>();
        if(createrIds != null) createrIds.forEach(list::add);
        return questionService.countQuestionNumOfCreater(list);
    }

    @Override
    public Map<Long, Integer> queryQuestionScores(Iterable<Long> ids) {
        List<Long> list = new ArrayList<>();
        if(ids != null) ids.forEach(list::add);
        return questionService.queryQuestionScores(list);
    }
}
