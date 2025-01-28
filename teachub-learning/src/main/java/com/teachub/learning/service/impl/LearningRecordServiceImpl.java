package com.teachub.learning.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teachub.api.dto.leanring.LearningLessonDTO;
import com.teachub.api.dto.leanring.LearningRecordDTO;
import com.teachub.common.exceptions.BizIllegalException;
import com.teachub.learning.domain.po.LearningLesson;
import com.teachub.learning.domain.po.LearningRecord;
import com.teachub.learning.mapper.LearningRecordMapper;
import com.teachub.learning.service.ILearningLessonService;
import com.teachub.learning.service.ILearningRecordService;
import groovy.lang.Lazy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 学习记录表 服务实现类
 * </p>
 *
 * @author Aeroeia
 * @since 2025-07-11
 */
@Service
@RequiredArgsConstructor
public class LearningRecordServiceImpl extends ServiceImpl<LearningRecordMapper, LearningRecord> implements ILearningRecordService {
    private final ILearningLessonService learningLessonService;
    @Override
    public LearningLessonDTO queryLearningRecordByCourse(Long userID, Long courseId) {
        LearningLesson lesson = learningLessonService.lambdaQuery()
                .eq(LearningLesson::getUserId, userID)
                .eq(LearningLesson::getCourseId, courseId)
                .one();
        if(lesson==null){
            throw new BizIllegalException("课表不存在该课程");
        }
        Long lessonId = lesson.getId();
        List<LearningRecord> list = this.lambdaQuery()
                .eq(LearningRecord::getLessonId, lessonId)
                .eq(LearningRecord::getUserId, userID)
                .list();
        List<LearningRecordDTO> records = list.stream()
                .map(r -> LearningRecordDTO.builder()
                        .sectionId(r.getSectionId())
                        .moment(r.getMoment())
                        .finished(r.getFinished())
                        .build()).collect(Collectors.toList());
        return LearningLessonDTO.builder()
                .id(lessonId)
                .latestSectionId(lesson.getLatestSectionId())
                .records(records)
                .build();
    }
}
