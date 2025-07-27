package com.teachub.learning.mq;

import com.teachub.common.constants.MqConstants;
import com.teachub.learning.enums.PointsRecordType;
import com.teachub.learning.mq.msg.SignInMessage;
import com.teachub.learning.service.IPointsRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LearningPointListener {
    private final IPointsRecordService pointsRecordService;
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "sign.points.queue",durable = "true"),
                    exchange = @Exchange(value = MqConstants.Exchange.LEARNING_EXCHANGE, type = ExchangeTypes.TOPIC),
                    key = MqConstants.Key.SIGN_IN
            )
    )
    public void listenSignInMessage(SignInMessage signInMessage){
        log.info("监听到签到消息:{}",signInMessage);
        if(signInMessage.getUserId()==null){
            log.error("用户id不存在");
            return;
        }
        pointsRecordService.addPoints(signInMessage, PointsRecordType.SIGN);

    }

    // 监听新增互动问答事件
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "qa.points.queue", durable = "true"),
            exchange = @Exchange(name = MqConstants.Exchange.LEARNING_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = MqConstants.Key.WRITE_REPLY
    ))
    public void listenWriteReplyMessage(Long userId){
        log.info("监听到新增互动问答事件,用户id:{}",userId);
    }
}
