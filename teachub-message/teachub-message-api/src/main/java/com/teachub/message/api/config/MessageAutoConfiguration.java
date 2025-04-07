package com.teachub.message.api.config;

import com.teachub.common.autoconfigure.mq.RocketMqHelper;
import com.teachub.message.api.client.AsyncSmsClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageAutoConfiguration {

    @Bean
    public AsyncSmsClient smsClient(RocketMqHelper mqHelper){
        return new AsyncSmsClient(mqHelper);
    }
}
