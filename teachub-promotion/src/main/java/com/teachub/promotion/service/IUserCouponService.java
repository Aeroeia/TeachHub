package com.teachub.promotion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teachub.promotion.domain.po.UserCoupon;

/**
 * <p>
 * 用户领取优惠券的记录，是真正使用的优惠券信息 服务类
 * </p>
 *
 * @author Aeroeia
 * @since 2025-08-01
 */
public interface IUserCouponService extends IService<UserCoupon> {

    void receiveCoupon(Long id);
}
