package com.teachub.promotion.service.dubbo;

import com.teachub.api.dto.promotion.CouponDiscountDTO;
import com.teachub.api.dto.promotion.OrderCourseDTO;
import com.teachub.api.promotion.PromotionClient;
import com.teachub.common.utils.BeanUtils;
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
        // 1. Convert API DTOs to Domain DTOs
        List<com.teachub.promotion.domain.dto.OrderCourseDTO> domainDTOS = BeanUtils.copyList(courseDTOS, com.teachub.promotion.domain.dto.OrderCourseDTO.class);
        
        // 2. Call service
        List<com.teachub.promotion.domain.dto.CouponDiscountDTO> domainResult = null;
        try {
            domainResult = userCouponService.findDiscountSolution(domainDTOS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return List.of();
        }

        // 3. Convert Domain DTOs back to API DTOs
        return BeanUtils.copyList(domainResult, CouponDiscountDTO.class);
    }
}
