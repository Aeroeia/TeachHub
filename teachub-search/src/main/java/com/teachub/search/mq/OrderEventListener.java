package com.teachub.search.mq;

import com.teachub.api.dto.trade.OrderBasicDTO;
import com.teachub.common.constants.MqConstants;
import com.teachub.common.utils.CollUtils;
import com.teachub.search.service.ICourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
public class OrderEventListener {

    @Component
    @RequiredArgsConstructor
    @RocketMQMessageListener(topic = MqConstants.Topic.ORDER_TOPIC, consumerGroup = "search_order_pay_consumer_group", selectorExpression = MqConstants.Tag.ORDER_PAY)
    public static class OrderPayListener implements RocketMQListener<OrderBasicDTO> {
        private final ICourseService courseService;

        @Override
        public void onMessage(OrderBasicDTO order) {
            if (order == null || order.getUserId() == null || CollUtils.isEmpty(order.getCourseIds())) {
                log.debug("订单支付，异常消息，信息未空");
                return;
            }
            log.debug("处理订单支付消息：{}", order);
            courseService.updateCourseSold(order.getCourseIds(), 1);
        }
    }

    @Component
    @RequiredArgsConstructor
    @RocketMQMessageListener(topic = MqConstants.Topic.ORDER_TOPIC, consumerGroup = "search_order_refund_consumer_group", selectorExpression = MqConstants.Tag.ORDER_REFUND)
    public static class OrderRefundListener implements RocketMQListener<OrderBasicDTO> {
        private final ICourseService courseService;

        @Override
        public void onMessage(OrderBasicDTO order) {
            if (order == null || order.getUserId() == null || CollUtils.isEmpty(order.getCourseIds())) {
                log.debug("订单退款，异常消息，信息未空");
                return;
            }
            log.debug("处理订单退款消息：{}", order);
            courseService.updateCourseSold(order.getCourseIds(), -1);
        }
    }
}
