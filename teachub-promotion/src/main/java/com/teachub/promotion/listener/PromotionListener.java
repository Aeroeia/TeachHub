package com.teachub.promotion.listener;

import com.teachub.common.constants.MqConstants;
import com.teachub.promotion.domain.dto.UserCouponDTO;
import com.teachub.promotion.domain.po.Coupon;
import com.teachub.promotion.mapper.CouponMapper;
import com.teachub.promotion.service.IUserCouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class PromotionListener {
    private final IUserCouponService userCouponService;
    private final CouponMapper couponMapper;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "coupon.receive.queue",durable = "true"),
            exchange = @Exchange(value = MqConstants.Exchange.PROMOTION_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = MqConstants.Key.COUPON_RECEIVE
    ))
    @Transactional
    public void onMsg(UserCouponDTO couponDTO){
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
