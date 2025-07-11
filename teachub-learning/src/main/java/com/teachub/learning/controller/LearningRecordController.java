package com.teachub.learning.controller;


import com.teachub.api.dto.leanring.LearningLessonDTO;
import com.teachub.common.exceptions.BadRequestException;
import com.teachub.common.utils.UserContext;
import com.teachub.learning.service.ILearningRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 学习记录表 前端控制器
 * </p>
 *
 * @author Aeroeia
 * @since 2025-07-11
 */
@Slf4j
@RestController
@RequestMapping("/learning-records")
@RequiredArgsConstructor
@Api(tags = "学习记录接口")
public class LearningRecordController {
    private final ILearningRecordService learningRecordService;
    /**
     * 查询当前用户指定课程的学习进度
     * @param courseId 课程id
     * @return 课表信息、学习记录及进度信息
     */
    @ApiOperation("查看用户指定课程学习记录")
    @GetMapping("/course/{courseId}")
    LearningLessonDTO queryLearningRecordByCourse(@PathVariable("courseId") Long courseId){
        Long userID = UserContext.getUser();
        if(userID==null){
            throw new BadRequestException("获取用户id失败");
        }
        log.info("课程id:{}",courseId);
        if(courseId==null){
            throw new BadRequestException("课程id为空");
        }
        return learningRecordService.queryLearningRecordByCourse(userID,courseId);
    }
}
