package com.teachub.api.promotion;

import com.teachub.api.dto.promotion.CouponDiscountDTO;
import com.teachub.api.dto.promotion.OrderCourseDTO;
import java.util.List;

public interface PromotionClient {
    List<CouponDiscountDTO> findDiscountSolution(List<OrderCourseDTO> courseDTOS);
}
