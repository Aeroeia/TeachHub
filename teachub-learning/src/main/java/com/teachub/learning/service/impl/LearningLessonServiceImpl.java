package com.teachub.learning.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teachub.api.client.course.CourseClient;
import com.teachub.api.dto.course.CourseSimpleInfoDTO;
import com.teachub.common.constants.Constant;
import com.teachub.common.domain.dto.PageDTO;
import com.teachub.common.domain.query.PageQuery;
import com.teachub.common.utils.CollUtils;
import com.teachub.learning.domain.po.LearningLesson;
import com.teachub.learning.domain.vo.LearningLessonVO;
import com.teachub.learning.mapper.LearningLessonMapper;
import com.teachub.learning.service.ILearningLessonService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    public PageDTO<LearningLessonVO> queryMyLesson(Long userId, PageQuery pageQuery) {
        Page<LearningLesson> page = pageQuery.toMpPage(Constant.LATEST_LEARN_TIME,false);
        Page<LearningLesson> pages = this.lambdaQuery().eq(LearningLesson::getUserId, userId)
                .page(page);
        List<LearningLesson> lessons = pages.getRecords();
        if(CollUtils.isEmpty(lessons)){
            return PageDTO.empty(pages);
        }
        List<Long> courseIds = lessons.stream().map(LearningLesson::getCourseId).collect(Collectors.toList());
        List<CourseSimpleInfoDTO> simpleInfoList = courseClient.getSimpleInfoList(courseIds);
        Map<Long, CourseSimpleInfoDTO> map = simpleInfoList.stream().collect(Collectors.toMap(CourseSimpleInfoDTO::getId, c -> c));
        List<LearningLessonVO> result = new ArrayList<>();
        for (LearningLesson lesson : lessons) {
            LearningLessonVO learningLessonVO = BeanUtil.copyProperties(lesson, LearningLessonVO.class);
            CourseSimpleInfoDTO courseSimpleInfoDTO = map.get(lesson.getCourseId());
            learningLessonVO.setCourseName(courseSimpleInfoDTO.getName());
            learningLessonVO.setCourseCoverUrl(courseSimpleInfoDTO.getCoverUrl());
            learningLessonVO.setSections(courseSimpleInfoDTO.getSectionNum());
            result.add(learningLessonVO);
        }
        return PageDTO.of(pages, result);
    }
}