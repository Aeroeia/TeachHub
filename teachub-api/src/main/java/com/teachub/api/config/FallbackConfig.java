package com.teachub.api.config;

import com.teachub.api.client.learning.fallback.LearningClientFallback;
import com.teachub.api.client.remark.fallback.RemarkClientFallBackFactory;
import com.teachub.api.client.trade.fallback.TradeClientFallback;
import com.teachub.api.client.user.fallback.UserClientFallback;
import com.teachub.api.promotion.fallback.PromotionFallBackFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FallbackConfig {
    @Bean
    public LearningClientFallback learningClientFallback(){
        return new LearningClientFallback();
    }

    @Bean
    public TradeClientFallback tradeClientFallback(){
        return new TradeClientFallback();
    }

    @Bean
    public UserClientFallback userClientFallback(){
        return new UserClientFallback();
    }

    @Bean
    public RemarkClientFallBackFactory remarkClientFallBackFactory(){
        return new RemarkClientFallBackFactory();
    }

    @Bean
    public PromotionFallBackFactory promotionFallBackFactory(){
        return new PromotionFallBackFactory();
    }

}
