package com.teachub.learning.service.impl;

import com.teachub.api.client.course.CourseClient;
import com.teachub.api.dto.course.CourseSimpleInfoDTO;
import com.teachub.learning.domain.po.LearningLesson;
import com.teachub.learning.mapper.LearningLessonMapper;
import com.teachub.learning.service.ILearningLessonService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 学生课程表 服务实现类
 * </p>
 *
 * @author aer
 * @since 2025-06-26
 */
@Service
@RequiredArgsConstructor
public class LearningLessonServiceImpl extends ServiceImpl<LearningLessonMapper, LearningLesson> implements ILearningLessonService {
    private final CourseClient courseClient;
    @Override
    public void saveLearningLeesons(Long userId, List<Long> ids) {
        //1.远程调用course微服务得到课程信息
        List<CourseSimpleInfoDTO> simpleInfoList = courseClient.getSimpleInfoList(ids);
        //2.封装po实体类，填充过期时间
        List<LearningLesson> lessons = new ArrayList<>();
        for(CourseSimpleInfoDTO info : simpleInfoList){
            LocalDateTime expireTime = LocalDateTime.now().plusMonths(info.getValidDuration());
            LearningLesson lesson = LearningLesson.builder()
                    .courseId(info.getId())
                    .userId(userId)
                    .expireTime(expireTime)
                    .createTime(LocalDateTime.now())
                    .build();
            lessons.add(lesson);
        }
        //3.批量保存
        this.saveBatch(lessons);
    }
}