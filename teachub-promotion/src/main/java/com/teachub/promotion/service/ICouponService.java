package com.teachub.promotion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teachub.promotion.domain.dto.CouponFormDTO;
import com.teachub.promotion.domain.po.Coupon;

/**
 * <p>
 * 优惠券的规则信息 服务类
 * </p>
 *
 * @author Aeroeia
 * @since 2025-07-30
 */
public interface ICouponService extends IService<Coupon> {

    void addCoupon(CouponFormDTO coupon);
}
