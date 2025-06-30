package com.teachub.learning.controller;


import com.teachub.common.domain.dto.PageDTO;
import com.teachub.common.domain.query.PageQuery;
import com.teachub.common.exceptions.BadRequestException;
import com.teachub.common.utils.UserContext;
import com.teachub.learning.domain.vo.LearningLessonVO;
import com.teachub.learning.service.ILearningLessonService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

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
}
