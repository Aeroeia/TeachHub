package com.teachub.learning.service;

import com.teachub.api.dto.leanring.LearningLessonDTO;
import com.teachub.learning.domain.dto.LearningRecordFormDTO;
import com.teachub.learning.domain.po.LearningRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 学习记录表 服务类
 * </p>
 *
 * @author Aeroeia
 * @since 2025-07-11
 */
public interface ILearningRecordService extends IService<LearningRecord> {

    LearningLessonDTO queryLearningRecordByCourse(Long userID, Long courseId);

    void commitLearningRecord(LearningRecordFormDTO learningRecordFormDTO);

}
