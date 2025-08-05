package com.teachub.promotion.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teachub.promotion.domain.po.Coupon;
import com.teachub.promotion.domain.po.UserCoupon;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 用户领取优惠券的记录，是真正使用的优惠券信息 Mapper 接口
 * </p>
 *
 * @author Aeroeia
 * @since 2025-08-01
 */
public interface UserCouponMapper extends BaseMapper<UserCoupon> {
    List<Coupon> queryMyCoupon(@Param("userId") Long userId);

}
