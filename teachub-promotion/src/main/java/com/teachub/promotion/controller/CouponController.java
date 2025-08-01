package com.teachub.promotion.controller;


import com.teachub.common.domain.dto.PageDTO;
import com.teachub.common.exceptions.BadRequestException;
import com.teachub.promotion.domain.dto.CouponFormDTO;
import com.teachub.promotion.domain.dto.CouponIssueFormDTO;
import com.teachub.promotion.domain.dto.CouponQuery;
import com.teachub.promotion.domain.vo.CouponDetailVO;
import com.teachub.promotion.domain.vo.CouponPageVO;
import com.teachub.promotion.service.ICouponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    @ApiOperation("新增优惠券-管理端")
    public void addCoupon(@RequestBody @Validated CouponFormDTO coupon) {
        log.info("新增优惠券：{}", coupon);
        couponService.addCoupon(coupon);
    }

    @PutMapping("/{id}")
    @ApiOperation("更新优惠券-管理端")
    public void updateCoupon(@RequestBody CouponFormDTO coupon, @PathVariable Long id) {
        log.info("更新优惠券：{}", coupon);
        couponService.updateCoupon(coupon, id);
    }

    @GetMapping("/page")
    @ApiOperation("分页查询优惠券-管理端")
    public PageDTO<CouponPageVO> queryCouponPage(CouponQuery query) {
        log.info("分页查询优惠券：{}", query);
        return couponService.queryCouponPage(query);
    }

    @PutMapping("/{id}/issue")
    @ApiOperation("发放优惠券")
    public void issueCoupon(@PathVariable Long id, @Validated @RequestBody CouponIssueFormDTO couponIssueFormDTO) {
        log.info("发放优惠券：{}", id);
        if (id == null) {
            throw new BadRequestException("优惠券id为空");
        }
        couponService.issueCoupon(id, couponIssueFormDTO);
    }

    @GetMapping("/{id}")
    @ApiOperation("查询优惠券详情")
    public CouponDetailVO queryCouponDetail(@PathVariable Long id) {
        log.info("查询优惠券详情：{}", id);
        return couponService.queryCouponDetail(id);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除优惠券")
    public void deleteCoupon(@PathVariable Long id) {
        log.info("删除优惠券");
        couponService.delete(id);
    }
    @ApiOperation("暂停发放优惠券")
    @PutMapping("/{id}/pause")
    public void pauseCoupon(@PathVariable Long id) {
        log.info("暂停发放优惠券");
        couponService.pauseCoupon(id);
    }

}
