package com.teachub.common.autoconfigure.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;

import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
public class RocketMqHelper {

    private final RocketMQTemplate rocketMQTemplate;

    public <T> void send(String topic, String tag, T t) {
        String destination = topic + ":" + tag;
        log.debug("准备发送消息，destination：{}， message：{}", destination, t);
        rocketMQTemplate.convertAndSend(destination, t);
    }

    public <T> void sendDelayMessage(String topic, String tag, T t, Duration delay) {
        String destination = topic + ":" + tag;
        int delayLevel = mapDelayToLevel(delay);
        log.debug("准备发送延迟消息，destination：{}， delayLevel：{}， message：{}", destination, delayLevel, t);
        rocketMQTemplate.syncSend(destination, MessageBuilder.withPayload(t).build(), 3000, delayLevel);
    }

    public <T> void sendAsync(String topic, String tag, T t) {
        String destination = topic + ":" + tag;
        log.debug("准备异步发送消息，destination：{}， message：{}", destination, t);
        rocketMQTemplate.asyncSend(destination, t, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.debug("发送消息成功，result: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("发送消息失败", throwable);
            }
        });
    }

    public <T> void sendAsync(String topic, String tag, T t, Long time) {
        if (time != null && time > 0) {
            String destination = topic + ":" + tag;
            int delayLevel = mapDelayToLevel(Duration.ofMillis(time));
            log.debug("准备异步发送延迟消息，destination：{}， delayLevel：{}， message：{}", destination, delayLevel, t);
            rocketMQTemplate.asyncSend(destination, MessageBuilder.withPayload(t).build(), new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.debug("发送消息成功，result: {}", sendResult);
                }

                @Override
                public void onException(Throwable throwable) {
                    log.error("发送消息失败", throwable);
                }
            }, 3000, delayLevel);
        } else {
            sendAsync(topic, tag, t);
        }
    }

    private int mapDelayToLevel(Duration delay) {
        long seconds = delay.getSeconds();
        if (seconds <= 1) return 1;
        if (seconds <= 5) return 2;
        if (seconds <= 10) return 3;
        if (seconds <= 30) return 4;
        if (seconds <= 60) return 5;
        if (seconds <= 120) return 6;
        if (seconds <= 180) return 7;
        if (seconds <= 240) return 8;
        if (seconds <= 300) return 9;
        if (seconds <= 360) return 10;
        if (seconds <= 420) return 11;
        if (seconds <= 480) return 12;
        if (seconds <= 540) return 13;
        if (seconds <= 600) return 14;
        if (seconds <= 1200) return 15;
        if (seconds <= 1800) return 16;
        if (seconds <= 3600) return 17;
        return 18;
    }
}
