package com.teachub.learning.service.impl;


import cn.hutool.db.DbRuntimeException;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teachub.api.client.course.CourseClient;
import com.teachub.api.dto.course.CourseFullInfoDTO;
import com.teachub.api.dto.leanring.LearningLessonDTO;
import com.teachub.api.dto.leanring.LearningRecordDTO;
import com.teachub.common.exceptions.BadRequestException;
import com.teachub.common.exceptions.BizIllegalException;
import com.teachub.common.utils.BeanUtils;
import com.teachub.common.utils.UserContext;
import com.teachub.learning.domain.dto.LearningRecordFormDTO;
import com.teachub.learning.domain.po.LearningLesson;
import com.teachub.learning.domain.po.LearningRecord;
import com.teachub.learning.enums.LessonStatus;
import com.teachub.learning.enums.SectionType;
import com.teachub.learning.mapper.LearningRecordMapper;
import com.teachub.learning.service.ILearningLessonService;
import com.teachub.learning.service.ILearningRecordService;
import groovy.lang.Lazy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private final CourseClient courseClient;

    @Override
    public LearningLessonDTO queryLearningRecordByCourse(Long userID, Long courseId) {
        LearningLesson lesson = learningLessonService.lambdaQuery()
                .eq(LearningLesson::getUserId, userID)
                .eq(LearningLesson::getCourseId, courseId)
                .one();
        if (lesson == null) {
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

    @Override
    @Transactional
    public void commitLearningRecord(LearningRecordFormDTO learningRecordFormDTO) {
        //获取用户id
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new BadRequestException("获取用户id失败");
        }
        //提交学习记录
        SectionType sectionType = learningRecordFormDTO.getSectionType();
        boolean isFinished;
        //1.考试记录
        if (sectionType.equals(SectionType.EXAM)) {
            isFinished = handledExam(userId, learningRecordFormDTO);
        }
        //2.视频记录
        else {
            isFinished = handleVideo(userId, learningRecordFormDTO);
        }
        Long lessonId = learningRecordFormDTO.getLessonId();
        LearningLesson lesson = learningLessonService.lambdaQuery().eq(LearningLesson::getId, lessonId).one();
        if(lesson==null){
            throw new BizIllegalException("课程不存在");
        }
        Boolean isAllFinished = false;
        //更新学习记录
        if (isFinished) {
            CourseFullInfoDTO courseInfoById = courseClient.getCourseInfoById(lesson.getCourseId(), false, false);
            Integer sectionNum = courseInfoById.getSectionNum();
            Integer currentNum = lesson.getLearnedSections() + 1;
            if(currentNum>=sectionNum){
                isAllFinished = true;
            }
        }
        learningLessonService.lambdaUpdate().eq(LearningLesson::getId, lessonId)
                .set(lesson.getStatus()== LessonStatus.NOT_BEGIN,LearningLesson::getStatus,LessonStatus.LEARNING)
                .set(isAllFinished, LearningLesson::getStatus, LessonStatus.FINISHED)
                .set(LearningLesson::getLatestLearnTime, learningRecordFormDTO.getCommitTime())
                .set(LearningLesson::getLatestSectionId, learningRecordFormDTO.getSectionId())
                .setSql(isFinished,"learned_sections=learned_sections+1") //原子性
                .update();
    }

    //处理考试提交记录
    private boolean handledExam(Long userId, LearningRecordFormDTO learningRecordFormDTO) {
        //dto转po
        LearningRecord learningRecord = BeanUtils.copyBean(learningRecordFormDTO, LearningRecord.class);
        learningRecord.setFinished(true);
        learningRecord.setUserId(userId);
        learningRecord.setFinishTime(learningRecordFormDTO.getCommitTime());
        boolean result = this.saveOrUpdate(learningRecord);
        if (!result) {
            throw new DbRuntimeException("新增考试记录失败");
        }
        return true;
    }

    //处理视频记录
    private boolean handleVideo(Long userId, LearningRecordFormDTO learningRecordFormDTO) {
        //查询是否有记录
        LearningRecord learningRecord = this.lambdaQuery().eq(LearningRecord::getLessonId, learningRecordFormDTO.getLessonId())
                .eq(LearningRecord::getSectionId, learningRecordFormDTO.getSectionId())
                .one();
        //判断是否学完
        boolean finished = false;
        if(learningRecordFormDTO.getMoment()!=null){
            finished = learningRecordFormDTO.getMoment() * 2 >= learningRecordFormDTO.getDuration();
        }
        //课程不存在新增
        if(learningRecord==null){
            learningRecord = BeanUtils.copyBean(learningRecordFormDTO, LearningRecord.class);
            learningRecord.setFinishTime(finished ? LocalDateTime.now() : null);
            learningRecord.setFinished(finished);
            learningRecord.setUpdateTime(learningRecordFormDTO.getCommitTime());
            learningRecord.setUserId(userId);
            if(learningRecordFormDTO.getMoment()!=null){
                learningRecord.setMoment(learningRecordFormDTO.getMoment());
            }
            this.save(learningRecord);
        }
        //课程存在更新
        else{
            //判断是否首次完成
            learningRecord.setFinishTime(!learningRecord.getFinished()&&finished ? LocalDateTime.now() : learningRecord.getFinishTime());
            learningRecord.setFinished(finished);
            learningRecord.setUpdateTime(learningRecordFormDTO.getCommitTime());
            if(learningRecordFormDTO.getMoment()!=null){
                learningRecord.setMoment(learningRecordFormDTO.getMoment());
            }
            this.lambdaUpdate().eq(LearningRecord::getId, learningRecord.getId()).update(learningRecord);
        }
        return finished;
    }

}
