package com.teachub.learning.mq;

import com.teachub.common.constants.MqConstants;
import com.teachub.learning.enums.PointsRecordType;
import com.teachub.learning.mq.msg.SignInMessage;
import com.teachub.learning.service.IPointsRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
public class LearningPointListener {

    @Component
    @RequiredArgsConstructor
    @RocketMQMessageListener(topic = MqConstants.Topic.LEARNING_TOPIC, consumerGroup = "sign_points_consumer_group", selectorExpression = MqConstants.Tag.SIGN_IN)
    public static class SignInListener implements RocketMQListener<SignInMessage> {
        private final IPointsRecordService pointsRecordService;

        @Override
        public void onMessage(SignInMessage signInMessage){
            log.info("监听到签到消息:{}",signInMessage);
            if(signInMessage.getUserId()==null){
                log.error("用户id不存在");
                return;
            }
            pointsRecordService.addPoints(signInMessage, PointsRecordType.SIGN);
        }
    }

    // 监听新增互动问答事件
    @Component
    @RocketMQMessageListener(topic = MqConstants.Topic.LEARNING_TOPIC, consumerGroup = "qa_points_consumer_group", selectorExpression = MqConstants.Tag.WRITE_REPLY)
    public static class WriteReplyListener implements RocketMQListener<Long> {
        @Override
        public void onMessage(Long userId){
            log.info("监听到新增互动问答事件,用户id:{}",userId);
        }
    }
}
