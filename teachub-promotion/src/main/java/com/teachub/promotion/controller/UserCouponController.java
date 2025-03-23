package com.teachub.promotion.controller;

import com.teachub.promotion.service.IUserCouponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "用户优惠券接口")
@RequestMapping("user-coupons")
@Slf4j
@RequiredArgsConstructor
public class UserCouponController {
    private final IUserCouponService userCouponService;

    @PostMapping("/{id}/receive")
    @ApiOperation("领取优惠券")
    public void receiveCoupon(@PathVariable Long id) {
        log.info("用户领取优惠券，id：{}", id);
        userCouponService.receiveCoupon(id);
    }
    @PostMapping("/{code}/exchange")
    @ApiOperation("兑换码兑换优惠券")
    public void exchangeCode(@PathVariable String code) {
        log.info("用户兑换码兑换优惠券，id：{}", code);
        userCouponService.exchangeCode(code);
    }
}
