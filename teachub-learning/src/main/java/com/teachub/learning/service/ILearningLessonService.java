package com.teachub.learning.service;

import com.teachub.learning.domain.po.LearningLesson;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 学生课程表 服务类
 * </p>
 *
 * @author aer
 * @since 2025-06-26
 */
public interface ILearningLessonService extends IService<LearningLesson> {

    void saveLearningLeesons(Long userId, List<Long> courseIds);
}
