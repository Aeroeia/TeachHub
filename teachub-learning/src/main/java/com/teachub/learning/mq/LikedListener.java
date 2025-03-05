package com.teachub.learning.mq;

import com.teachub.api.dto.remark.LikedTimesDTO;
import com.teachub.common.constants.MqConstants;
import com.teachub.common.exceptions.BizIllegalException;
import com.teachub.common.utils.CollUtils;
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

import java.util.ArrayList;
import java.util.List;

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
    public void LikedTimesListener(List<LikedTimesDTO> list) {
        log.info("监听点赞数变化:{}",list);
        if(CollUtils.isEmpty(list)){
            return;
        }
        List<InteractionReply> interactionReplies = new ArrayList<>();
        for(LikedTimesDTO likedTimesDTO : list){
            InteractionReply interactionReply = new InteractionReply();
            interactionReply.setId(likedTimesDTO.getBizId());
            interactionReply.setLikedTimes(likedTimesDTO.getLikedTimes());
            interactionReplies.add(interactionReply);
        }
        boolean update = interactionReplyService.updateBatchById(interactionReplies);
        if(!update){
            throw new BizIllegalException("更新点赞失败");
        }
    }
}
