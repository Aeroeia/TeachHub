package com.teachub.learning.service;

import com.teachub.common.domain.dto.PageDTO;
import com.teachub.common.domain.query.PageQuery;
import com.teachub.learning.domain.po.LearningLesson;
import com.baomidou.mybatisplus.extension.service.IService;
import com.teachub.learning.domain.vo.LearningLessonVO;

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

    PageDTO<LearningLessonVO> queryMyLesson(Long userId, PageQuery pageQuery);

    LearningLessonVO getNowLesson(Long userId);

    void delete(Long userId, Long courseId);

    Long isLessonValid(Long userId, Long courseId);
}
