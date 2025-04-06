package com.teachub.common.autoconfigure.mq;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(RocketMQTemplate.class)
public class RocketMqConfig {

    @Bean
    @ConditionalOnMissingBean
    public RocketMqHelper rocketMqHelper(RocketMQTemplate rocketMQTemplate){
        return new RocketMqHelper(rocketMQTemplate);
    }
}
