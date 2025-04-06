package com.teachub.learning.mq;

import com.teachub.api.dto.remark.LikedTimesDTO;
import com.teachub.common.constants.MqConstants;
import com.teachub.common.exceptions.BizIllegalException;
import com.teachub.common.utils.CollUtils;
import com.teachub.learning.domain.po.InteractionReply;
import com.teachub.learning.service.IInteractionReplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = MqConstants.Topic.LIKE_RECORD_TOPIC, consumerGroup = "qa_liked_times_consumer_group", selectorExpression = MqConstants.Tag.QA_LIKED_TIMES_KEY)
public class LikedListener implements RocketMQListener<List<LikedTimesDTO>> {
    private final IInteractionReplyService interactionReplyService;

    @Override
    public void onMessage(List<LikedTimesDTO> list) {
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
