package com.teachub.api.client.learning;

import com.teachub.api.dto.leanring.LearningLessonDTO;

public interface LearningClient {

    /**
     * 统计课程学习人数
     * @param courseId 课程id
     * @return 学习人数
     */
    Integer countLearningLessonByCourse(Long courseId);

    /**
     * 校验当前用户是否可以学习当前课程
     * @param courseId 课程id
     * @return lessonId，如果是报名了则返回lessonId，否则返回空
     */
    Long isLessonValid(Long courseId);

    /**
     * 查询当前用户指定课程的学习进度
     * @param courseId 课程id
     * @return 课表信息、学习记录及进度信息
     */
    LearningLessonDTO queryLearningRecordByCourse(Long courseId);

}
