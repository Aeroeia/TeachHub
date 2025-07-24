package com.teachub.learning.mq;

import com.teachub.api.dto.remark.LikedTimesDTO;
import com.teachub.common.constants.MqConstants;
import com.teachub.learning.domain.po.InteractionReply;
import com.teachub.learning.service.IInteractionReplyService;
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
public class LikedListener {
    private final IInteractionReplyService interactionReplyService;
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = "qa.liked.times.queue", durable = "true"),
                    exchange = @Exchange(name = MqConstants.Exchange.LIKE_RECORD_EXCHANGE, type = ExchangeTypes.TOPIC),
                    key = MqConstants.Key.QA_LIKED_TIMES_KEY
            ))
    public void LikedTimesListener(LikedTimesDTO likedTimesDTO) {
        log.info("监听点赞数变化id:{}",likedTimesDTO);
        if(likedTimesDTO.getIsLiked()){
            interactionReplyService.lambdaUpdate()
                    .eq(InteractionReply::getId,likedTimesDTO.getBizId())
                    .setSql("liked_times=liked_times+1")
                    .update();
        }
        else{
            interactionReplyService.lambdaUpdate()
                    .setSql("liked_times=liked_times-1")
                    .update();
        }
    }
}
