package com.teachub.promotion.controller;


import com.teachub.promotion.domain.dto.CouponFormDTO;
import com.teachub.promotion.service.ICouponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

/**
 * <p>
 * 优惠券的规则信息 前端控制器
 * </p>
 *
 * @author Aeroeia
 * @since 2025-07-30
 */
@RestController
@RequestMapping("/coupons")
@Api(tags = "优惠券相关接口")
@RequiredArgsConstructor
@Slf4j
public class CouponController {
    private final ICouponService couponService;
    @PostMapping
    @ApiOperation("新增优惠券")
    public void addCoupon(@RequestBody @Validated CouponFormDTO coupon) {
        log.info("新增优惠券：{}", coupon);
        couponService.addCoupon(coupon);
    }
}
