package com.teachub.learning.service.dubbo;

import com.teachub.api.client.learning.LearningClient;
import com.teachub.api.dto.leanring.LearningLessonDTO;
import com.teachub.common.utils.UserContext;
import com.teachub.learning.service.ILearningLessonService;
import com.teachub.learning.service.ILearningRecordService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

@DubboService
public class LearningDubboServiceImpl implements LearningClient {

    @Autowired
    private ILearningLessonService learningLessonService;
    @Autowired
    private ILearningRecordService learningRecordService;

    @Override
    public Integer countLearningLessonByCourse(Long courseId) {
        return learningLessonService.countLearningLessonByCourse(courseId);
    }

    @Override
    public Long isLessonValid(Long courseId) {
        Long userId = UserContext.getUser();
        if(userId == null){
            return null;
        }
        return learningLessonService.isLessonValid(userId, courseId);
    }

    @Override
    public LearningLessonDTO queryLearningRecordByCourse(Long courseId) {
        Long userId = UserContext.getUser();
        if(userId == null){
            return null;
        }
        return learningRecordService.queryLearningRecordByCourse(userId, courseId);
    }
}
