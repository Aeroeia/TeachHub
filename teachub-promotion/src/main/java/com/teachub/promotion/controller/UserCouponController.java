package com.teachub.promotion.controller;

import com.teachub.common.domain.dto.PageDTO;
import com.teachub.promotion.domain.dto.CouponDiscountDTO;
import com.teachub.promotion.domain.dto.CouponQuery;
import com.teachub.promotion.domain.dto.OrderCourseDTO;
import com.teachub.promotion.domain.vo.CouponVO;
import com.teachub.promotion.service.IUserCouponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @ApiOperation("查询我的优惠券")
    @GetMapping("/page")
    public PageDTO<CouponVO> queryMyCoupons(CouponQuery couponQuery){
        log.info("查询我的优惠券：{}", couponQuery);
        return userCouponService.queryMyCoupons(couponQuery);
    }
    @ApiOperation("查询可用优惠券方案")
    @PostMapping("/available")
    public List<CouponDiscountDTO> findDiscountSolution(@RequestBody List<OrderCourseDTO> courseDTOS) throws InterruptedException {
        log.info("查询可用优惠券方案：{}", courseDTOS);
        return userCouponService.findDiscountSolution(courseDTOS);
    }
}
