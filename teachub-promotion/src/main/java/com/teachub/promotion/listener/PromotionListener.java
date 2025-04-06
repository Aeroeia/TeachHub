package com.teachub.promotion.listener;

import com.teachub.common.constants.MqConstants;
import com.teachub.promotion.domain.dto.UserCouponDTO;
import com.teachub.promotion.domain.po.Coupon;
import com.teachub.promotion.mapper.CouponMapper;
import com.teachub.promotion.service.IUserCouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
@RocketMQMessageListener(topic = MqConstants.Topic.PROMOTION_TOPIC, consumerGroup = "coupon_receive_consumer_group", selectorExpression = MqConstants.Tag.COUPON_RECEIVE)
public class PromotionListener implements RocketMQListener<UserCouponDTO> {
    private final IUserCouponService userCouponService;
    private final CouponMapper couponMapper;

    @Override
    @Transactional
    public void onMessage(UserCouponDTO couponDTO){
        log.info("收到优惠券领取消息：{}", couponDTO);
        //查询并更新优惠券领券数
        Coupon coupon = couponMapper.selectById(couponDTO.getCouponId());
        if(coupon==null){
            log.error("没有此优惠券信息");
            return;
        }
        couponMapper.updateIssueNum(couponDTO.getCouponId());
        //更新用户优惠券信息
        userCouponService.saveCoupon(couponDTO);
    }
}
