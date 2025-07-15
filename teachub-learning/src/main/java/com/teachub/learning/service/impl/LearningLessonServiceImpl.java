package com.teachub.learning.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teachub.api.client.course.CatalogueClient;
import com.teachub.api.client.course.CourseClient;
import com.teachub.api.dto.course.CataSimpleInfoDTO;
import com.teachub.api.dto.course.CourseFullInfoDTO;
import com.teachub.api.dto.course.CourseSimpleInfoDTO;
import com.teachub.common.constants.Constant;
import com.teachub.common.domain.dto.PageDTO;
import com.teachub.common.domain.query.PageQuery;
import com.teachub.common.exceptions.BadRequestException;
import com.teachub.common.exceptions.BizIllegalException;
import com.teachub.common.utils.CollUtils;
import com.teachub.common.utils.DateUtils;
import com.teachub.common.utils.UserContext;
import com.teachub.learning.domain.dto.LearningPlanDTO;
import com.teachub.learning.domain.po.LearningLesson;
import com.teachub.learning.domain.po.LearningRecord;
import com.teachub.learning.domain.vo.LearningLessonVO;
import com.teachub.learning.domain.vo.LearningPlanPageVO;
import com.teachub.learning.domain.vo.LearningPlanVO;
import com.teachub.learning.enums.LessonStatus;
import com.teachub.learning.enums.PlanStatus;
import com.teachub.learning.mapper.LearningLessonMapper;
import com.teachub.learning.service.ILearningLessonService;
import com.teachub.learning.service.ILearningRecordService;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
@Slf4j
public class LearningLessonServiceImpl extends ServiceImpl<LearningLessonMapper, LearningLesson> implements ILearningLessonService {
    private final CourseClient courseClient;
    private final CatalogueClient catalogueClient;
    @Lazy
    @Autowired
    private ILearningRecordService learningRecordService;
    @Override
    public void saveLearningLeesons(Long userId, List<Long> ids) {
        //1.远程调用course微服务得到课程信息
        List<CourseSimpleInfoDTO> simpleInfoList = courseClient.getSimpleInfoList(ids);
        //2.封装po实体类，填充过期时间
        List<LearningLesson> lessons = new ArrayList<>();
        for (CourseSimpleInfoDTO info : simpleInfoList) {
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

    @Override
    @SuppressWarnings("unchecked")
    public LearningLessonVO getNowLesson(Long userId) {
        LearningLesson learningLesson = this.lambdaQuery().eq(LearningLesson::getUserId, userId)
                .eq(LearningLesson::getStatus, LessonStatus.LEARNING)
                .orderByDesc(LearningLesson::getLatestLearnTime)
                .last("limit 1")
                .one();
        if(learningLesson == null){
            return null;
        }
        Integer count = this.lambdaQuery().eq(LearningLesson::getUserId, userId)
                .count();
        CourseFullInfoDTO courseInfo = courseClient.getCourseInfoById(learningLesson.getCourseId(), false, false);
        if(courseInfo==null){
            throw new BizIllegalException("课程不存在");
        }
        LearningLessonVO learningLessonVO = BeanUtil.copyProperties(learningLesson, LearningLessonVO.class);
        learningLessonVO.setCourseName(courseInfo.getName());
        learningLessonVO.setSections(courseInfo.getSectionNum());
        learningLessonVO.setCourseCoverUrl(courseInfo.getCoverUrl());
        learningLessonVO.setCourseAmount(count);
        Long latestSectionId = learningLesson.getLatestSectionId();
        List<CataSimpleInfoDTO> cataSimpleInfoDTOS = catalogueClient.batchQueryCatalogue(List.of(latestSectionId));
        if(CollUtils.isEmpty(cataSimpleInfoDTOS)){
            throw new BizIllegalException("小节不存在");
        }
        CataSimpleInfoDTO cataSimpleInfoDTO = cataSimpleInfoDTOS.get(0);
        learningLessonVO.setLatestSectionIndex(cataSimpleInfoDTO.getCIndex());
        learningLessonVO.setLatestSectionName(cataSimpleInfoDTO.getName());
        return learningLessonVO;

    }

    @Override
    public void delete(Long userId, Long courseId) {
        log.info("课程删除");
        LearningLesson lesson = this.lambdaQuery().eq(LearningLesson::getUserId, userId)
                .eq(LearningLesson::getCourseId, courseId)
                .one();
        if(lesson==null){
            return;
        }
        log.info("课程:{}",lesson);
        this.lambdaUpdate().eq(LearningLesson::getCourseId,courseId)
                .eq(LearningLesson::getUserId,userId)
                .remove();
    }

    @Override
    public Long isLessonValid(Long userId, Long courseId) {
        LearningLesson lesson = this.lambdaQuery().eq(LearningLesson::getCourseId, courseId)
                .eq(LearningLesson::getUserId, userId)
                .one();
        if(lesson==null){
            throw new BizIllegalException("课程不存在");
        }
        if(lesson.getStatus()==LessonStatus.EXPIRED){
            throw new BizIllegalException("课程已过期");
        }
        return lesson.getId();
    }

    @Override
    public LearningLessonVO queryLearningRecordByCourse(Long userId, Long courseId) {
        LearningLesson lesson = this.lambdaQuery().eq(LearningLesson::getUserId, userId)
                .eq(LearningLesson::getCourseId, courseId)
                .one();
        if(lesson==null){
            return null;
        }
        return LearningLessonVO.builder()
                .id(lesson.getId())
                .courseId(lesson.getCourseId())
                .status(lesson.getStatus())
                .learnedSections(lesson.getLearnedSections())
                .createTime(lesson.getCreateTime())
                .expireTime(lesson.getExpireTime())
                .planStatus(lesson.getPlanStatus())
                .build();
    }

    @Override
    public Integer countLearningLessonByCourse(Long courseId) {
        return this.lambdaQuery().eq(LearningLesson::getCourseId, courseId)
                .count();
    }
    /*
    更新过期时间
     */
    @Override
    public void updateExpiredLessons() {
        List<LearningLesson> list = this.list();
        List<Long> updateList = new ArrayList<>();
        for(LearningLesson lesson:list){
            LocalDateTime now = LocalDateTime.now();
            if(now.isAfter(lesson.getExpireTime())){
                updateList.add(lesson.getId());
            }
        }
        this.lambdaUpdate().in(LearningLesson::getId,updateList)
                .set(LearningLesson::getStatus,LessonStatus.EXPIRED);
    }

    @Override
    public void savePlans(LearningPlanDTO learningPlanDTO) {
        Long userId = UserContext.getUser();
        if(userId==null){
            throw new BadRequestException("获取用户id失败");
        }
        Long courseId = learningPlanDTO.getCourseId();
        this.lambdaUpdate().eq(LearningLesson::getUserId,userId)
                .eq(LearningLesson::getCourseId,courseId)
                .set(LearningLesson::getPlanStatus, PlanStatus.PLAN_RUNNING)
                .set(LearningLesson::getWeekFreq,learningPlanDTO.getFreq())
                .update();
    }

    @Override
    public LearningPlanPageVO queryMyPlan(PageQuery pageQuery) {
        Long userId = UserContext.getUser();
        if(userId==null){
            throw new BadRequestException("用户未登录");
        }
        //TODO 获取积分

        //分页
        Page<LearningLesson> page = pageQuery.toMpPageDefaultSortByCreateTimeDesc();
        //查询课表
        Page<LearningLesson> p = this.lambdaQuery().eq(LearningLesson::getUserId, userId)
                .in(LearningLesson::getStatus, LessonStatus.LEARNING, LessonStatus.NOT_BEGIN)
                .eq(LearningLesson::getPlanStatus, PlanStatus.PLAN_RUNNING)
                .page(page);
        List<LearningLesson> lessons = p.getRecords();
        if(CollUtils.isEmpty(lessons)){
            return null;
        }
        //获取本周总计划学习数量
        Integer totalPlans = lessons.stream().map(LearningLesson::getWeekFreq).reduce(0, Integer::sum);
        //获取本周学习记录
        List<Long> ids = lessons.stream().map(LearningLesson::getId).collect(Collectors.toList());
        LocalDate now = LocalDate.now();
        LocalDateTime weekBeginTime = DateUtils.getWeekBeginTime(now);
        LocalDateTime weekEndTime = DateUtils.getWeekEndTime(now);
        List<LearningRecord> learningRecords = learningRecordService.lambdaQuery().in(LearningRecord::getLessonId, ids)
                .eq(LearningRecord::getFinished, true)
                .between(LearningRecord::getUpdateTime, weekBeginTime, weekEndTime)
                .list();
        //获取本周学习总量
        Integer totalLearned = learningRecords.size();
        //获取课程信息
        List<Long> courseIds = lessons.stream().map(LearningLesson::getCourseId).collect(Collectors.toList());
        List<CourseSimpleInfoDTO> simpleInfoList = courseClient.getSimpleInfoList(courseIds);
        //对课程根据id分类
        Map<Long, CourseSimpleInfoDTO> courseMap = simpleInfoList.stream().collect(Collectors.toMap(CourseSimpleInfoDTO::getId, e -> e));
        //对学习记录根据课表id分类
        Map<Long, List<LearningRecord>> recordMap = learningRecords.stream().collect(Collectors.groupingBy(LearningRecord::getLessonId));
        List<LearningPlanVO> learningPlanVOS = new ArrayList<>();
        for(LearningLesson lesson :  lessons){
            Long lessonId = lesson.getId();
            LearningPlanVO learningPlanVO = new LearningPlanVO();
            learningPlanVO.setId(lessonId);
            learningPlanVO.setLearnedSections(lesson.getLearnedSections());
            learningPlanVO.setLatestLearnTime(lesson.getLatestLearnTime());
            learningPlanVO.setWeekLearnedSections(recordMap.get(lessonId)==null?0:recordMap.get(lessonId).size());
            learningPlanVO.setCourseId(lesson.getCourseId());
            learningPlanVO.setWeekFreq(lesson.getWeekFreq());
            learningPlanVO.setCourseName(courseMap.get(lesson.getCourseId()).getName());
            learningPlanVO.setSections(courseMap.get(lesson.getCourseId()).getSectionNum());
            learningPlanVOS.add(learningPlanVO);
        }
        LearningPlanPageVO learningPlanPageVO = new LearningPlanPageVO();
        learningPlanPageVO.setWeekTotalPlan(totalPlans);
        learningPlanPageVO.setWeekFinished(totalLearned);
        learningPlanPageVO.setPages(p.getPages());
        learningPlanPageVO.setTotal(p.getTotal());
        learningPlanPageVO.setList(learningPlanVOS);
        return learningPlanPageVO;
    }


}
