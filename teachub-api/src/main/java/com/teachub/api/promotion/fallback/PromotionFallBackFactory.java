package com.teachub.api.promotion.fallback;


import com.teachub.api.dto.promotion.CouponDiscountDTO;
import com.teachub.api.dto.promotion.OrderCourseDTO;
import com.teachub.api.promotion.PromotionClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.List;

@Slf4j
public class PromotionFallBackFactory implements FallbackFactory<PromotionClient> {

    @Override
    public PromotionClient create(Throwable cause) {
        log.error("远程调用promotion出现异常:",cause);
        return new PromotionClient() {
            @Override
            public List<CouponDiscountDTO> findDiscountSolution(List<OrderCourseDTO> courseDTOS) {
                return null;
            }
        };
    }
}
