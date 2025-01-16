package com.teachub.learning.mq;

import cn.hutool.core.collection.CollUtil;
import com.teachub.api.dto.trade.OrderBasicDTO;
import com.teachub.common.constants.MqConstants;
import com.teachub.learning.service.ILearningLessonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class LessonChangeListener {
    private final ILearningLessonService learningLessonService;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MqConstants.Queue.LEARNING_LESSON_PAY_QUEUE,durable = "true"),
            exchange = @Exchange(name = MqConstants.Exchange.ORDER_EXCHANGE,type = ExchangeTypes.TOPIC),
            key = MqConstants.Key.ORDER_PAY_KEY)
    )
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
