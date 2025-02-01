package com.teachub.learning.controller;


import com.teachub.common.domain.dto.PageDTO;
import com.teachub.common.domain.query.PageQuery;
import com.teachub.common.exceptions.BadRequestException;
import com.teachub.common.utils.UserContext;
import com.teachub.learning.domain.dto.LearningPlanDTO;
import com.teachub.learning.domain.vo.LearningLessonVO;
import com.teachub.learning.service.ILearningLessonService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 学生课程表 前端控制器
 * </p>
 *
 * @author aer
 * @since 2025-06-26
 */
@Slf4j
@Api(tags = "我的课程相关接口")
@RestController
@RequestMapping("/lessons")
@RequiredArgsConstructor
public class LearningLessonController {
    private final ILearningLessonService learningLessonService;
    @ApiOperation("分页查询课表")
    @GetMapping("/page")
    public PageDTO<LearningLessonVO> queryMyLessons(PageQuery pageQuery){
        Long userId = UserContext.getUser();
        if(userId == null){
            throw new BadRequestException("用户未登录");
        }
        log.info("当前用户id:{}",userId);
        log.info("分页参数:{}", pageQuery);
        PageDTO<LearningLessonVO> result = learningLessonService.queryMyLesson(userId, pageQuery);
        return result;
    }
    @ApiOperation("查询正在学习的课程")
    @GetMapping("/now")
    public LearningLessonVO getNowLesson(){
        Long userId = UserContext.getUser();
        log.info("当前用户id:{}",userId);
        if(userId==null){
            throw new BadRequestException("获取用户id失败");
        }
        LearningLessonVO result = learningLessonService.getNowLesson(userId);
        return result;
    }
    @ApiOperation("删除课程")
    @DeleteMapping("/{courseId}")
    public void delete(@PathVariable Long courseId){
        log.info("当前id:{}",courseId);
        Long userId = UserContext.getUser();
        log.info("当前用户ID:{}",userId);
        if(userId==null){
            throw new BadRequestException("获取用户id失败");
        }
        learningLessonService.delete(userId,courseId);
    }
    @ApiOperation("查询课程是否有效")
    @GetMapping("/{courseId}/valid")
    Long isLessonValid(@PathVariable("courseId") Long courseId){
        log.info("课程id:{}",courseId);
        if(courseId==null){
            throw new BadRequestException("获取课程id失败");
        }
        Long userId = UserContext.getUser();
        log.info("用户id:{}",userId);
        if(userId==null){
            throw new BadRequestException("获取用户id失败");
        }
        return learningLessonService.isLessonValid(userId,courseId);
    }
    @ApiOperation("查询当前用户指定课程状态")
    @GetMapping("/{courseId}")
    public LearningLessonVO queryLearningRecord(@PathVariable("courseId") Long courseId){
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new BadRequestException("获取用户id失败");
        }
        if(courseId==null){
            throw new BadRequestException("获取课程id失败");
        }
        return learningLessonService.queryLearningRecordByCourse(userId,courseId);
    }
    @ApiOperation("统计课程学习人数")
    @GetMapping("/{courseId}/count")
    public Integer countLearningLessonByCourse(@PathVariable("courseId") Long courseId){
        log.info("课程id:{}",courseId);
        if(courseId==null){
            throw new BadRequestException("获取课程id失败");
        }
        return learningLessonService.countLearningLessonByCourse(courseId);
    }
    @ApiOperation("创建学习计划")
    @PostMapping("/plans")
    public void savePlans(@RequestBody @Validated LearningPlanDTO learningPlanDTO){
        log.info("学习计划:{}",learningPlanDTO);
        learningLessonService.savePlans(learningPlanDTO);
    }
}
