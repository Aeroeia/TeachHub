package com.teachub.search.mq;

import com.teachub.common.constants.MqConstants;
import com.teachub.search.service.ICourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
public class CourseEventListener {

    @Component
    @RequiredArgsConstructor
    @RocketMQMessageListener(topic = MqConstants.Topic.COURSE_TOPIC, consumerGroup = "search_course_up_consumer_group", selectorExpression = MqConstants.Tag.COURSE_UP)
    public static class CourseUpListener implements RocketMQListener<Long> {
        private final ICourseService courseService;

        @Override
        public void onMessage(Long courseId){
            log.debug("监听到课程{}上架", courseId);
            courseService.handleCourseUp(courseId);
        }
    }

    @Component
    @RequiredArgsConstructor
    @RocketMQMessageListener(topic = MqConstants.Topic.COURSE_TOPIC, consumerGroup = "search_course_down_consumer_group", selectorExpression = MqConstants.Tag.COURSE_DOWN)
    public static class CourseDownListener implements RocketMQListener<Long> {
        private final ICourseService courseService;

        @Override
        public void onMessage(Long courseId){
            log.debug("监听到课程{}下架", courseId);
            courseService.handleCourseDelete(courseId);
        }
    }

    @Component
    @RequiredArgsConstructor
    @RocketMQMessageListener(topic = MqConstants.Topic.COURSE_TOPIC, consumerGroup = "search_course_expire_consumer_group", selectorExpression = MqConstants.Tag.COURSE_EXPIRE)
    public static class CourseExpireListener implements RocketMQListener<Long> {
        private final ICourseService courseService;

        @Override
        public void onMessage(Long courseId){
            courseService.handleCourseDelete(courseId);
        }
    }
}
