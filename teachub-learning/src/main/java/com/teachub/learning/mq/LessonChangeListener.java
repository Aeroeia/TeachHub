package com.teachub.learning.mq;

import cn.hutool.core.collection.CollUtil;
import com.teachub.api.dto.trade.OrderBasicDTO;
import com.teachub.common.constants.MqConstants;
import com.teachub.learning.service.ILearningLessonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
public class LessonChangeListener {

    @Component
    @RequiredArgsConstructor
    @RocketMQMessageListener(topic = MqConstants.Topic.ORDER_TOPIC, consumerGroup = "learning_lesson_pay_consumer_group", selectorExpression = MqConstants.Tag.ORDER_PAY)
    public static class PayListener implements RocketMQListener<OrderBasicDTO> {
        private final ILearningLessonService learningLessonService;

        @Override
        public void onMessage(OrderBasicDTO orderBasicDTO){
            log.info("收到报名通知：{}",orderBasicDTO);
            if(orderBasicDTO.getOrderId()==null||
                    orderBasicDTO.getUserId()==null||
                    CollUtil.isEmpty(orderBasicDTO.getCourseIds())){
                //不抛出异常 否则重试到结束
                return;
            }
            Long userId = orderBasicDTO.getUserId();
            List<Long> courseIds = orderBasicDTO.getCourseIds();
            learningLessonService.saveLearningLeesons(userId,courseIds);
        }
    }

    @Component
    @RequiredArgsConstructor
    @RocketMQMessageListener(topic = MqConstants.Topic.ORDER_TOPIC, consumerGroup = "learning_lesson_refund_consumer_group", selectorExpression = MqConstants.Tag.ORDER_REFUND)
    public static class RefundListener implements RocketMQListener<OrderBasicDTO> {
        private final ILearningLessonService learningLessonService;

        @Override
        public void onMessage(OrderBasicDTO orderBasicDTO){
            log.info("收到退课通知：{}",orderBasicDTO);
            if(orderBasicDTO==null){
                return;
            }
            Long userId = orderBasicDTO.getUserId();
            Long courseId = orderBasicDTO.getCourseIds().get(0);
            learningLessonService.delete(userId,courseId);
        }
    }
}
