package com.teachub.api.promotion;

import com.teachub.api.dto.promotion.CouponDiscountDTO;
import com.teachub.api.dto.promotion.OrderCourseDTO;
import com.teachub.api.promotion.fallback.PromotionFallBackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(value = "promotion-service",fallbackFactory = PromotionFallBackFactory.class)
public interface PromotionClient {
    @PostMapping("/user-coupons/available")
    List<CouponDiscountDTO> findDiscountSolution(@RequestBody List<OrderCourseDTO> courseDTOS);

}
