package com.teachub.course.handler;

import com.teachub.course.service.ICourseService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @ClassName CourseJobHandler
 *  wusongsong
 * @Date /9/16 15:04
 * @Version
 **/
@Component
@Slf4j
public class CourseJobHandler {

    @Autowired
    private ICourseService courseService;

    @XxlJob("courseFinished")
    public void courseFinished(){
        courseService.courseFinished();
    }
}
