package com.teachub.promotion.service.dubbo;

import com.teachub.api.dto.promotion.CouponDiscountDTO;
import com.teachub.api.dto.promotion.OrderCourseDTO;
import com.teachub.api.promotion.PromotionClient;
import com.teachub.promotion.service.IUserCouponService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@DubboService
public class PromotionDubboServiceImpl implements PromotionClient {

    @Autowired
    private IUserCouponService userCouponService;

    @Override
    public List<CouponDiscountDTO> findDiscountSolution(List<OrderCourseDTO> courseDTOS) {
        return userCouponService.findDiscountSolution(courseDTOS);
    }
}
