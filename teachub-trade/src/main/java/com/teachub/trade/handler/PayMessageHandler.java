package com.teachub.trade.handler;

import com.teachub.common.constants.MqConstants;
import com.teachub.pay.sdk.dto.PayResultDTO;
import com.teachub.pay.sdk.dto.RefundResultDTO;
import com.teachub.trade.domain.dto.OrderDelayQueryDTO;
import com.teachub.trade.service.IOrderService;
import com.teachub.trade.service.IPayService;
import com.teachub.trade.service.IRefundApplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
public class PayMessageHandler {

    @Component
    @RequiredArgsConstructor
    @RocketMQMessageListener(topic = MqConstants.Topic.PAY_TOPIC, consumerGroup = "trade_pay_success_consumer_group", selectorExpression = MqConstants.Tag.PAY_SUCCESS)
    public static class PaySuccessListener implements RocketMQListener<PayResultDTO> {
        private final IOrderService orderService;
        @Override
        public void onMessage(PayResultDTO payResult) {
            log.debug("收到支付成功通知：{}", payResult);
            orderService.handlePaySuccess(payResult);
        }
    }

    @Component
    @RequiredArgsConstructor
    @RocketMQMessageListener(topic = MqConstants.Topic.PAY_TOPIC, consumerGroup = "trade_refund_result_consumer_group", selectorExpression = MqConstants.Tag.REFUND_CHANGE)
    public static class RefundResultListener implements RocketMQListener<RefundResultDTO> {
        private final IRefundApplyService refundApplyService;
        @Override
        public void onMessage(RefundResultDTO refundResult) {
            log.debug("收到退款变更成功通知：{}", refundResult);
            refundApplyService.handleRefundResult(refundResult);
        }
    }

    @Component
    @RequiredArgsConstructor
    @RocketMQMessageListener(topic = MqConstants.Topic.TRADE_DELAY_TOPIC, consumerGroup = "trade_delay_order_query_consumer_group", selectorExpression = MqConstants.Tag.ORDER_DELAY)
    public static class OrderDelayQueryListener implements RocketMQListener<OrderDelayQueryDTO> {
        private final IPayService payService;
        @Override
        public void onMessage(OrderDelayQueryDTO message) {
            log.debug("收到订单延迟查询通知：{}", message);
            payService.queryPayResult(message);
        }
    }
}
