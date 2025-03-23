package com.teachub.promotion.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teachub.common.exceptions.BadRequestException;
import com.teachub.common.exceptions.BizIllegalException;
import com.teachub.common.utils.UserContext;
import com.teachub.promotion.domain.po.Coupon;
import com.teachub.promotion.domain.po.UserCoupon;
import com.teachub.promotion.enums.CouponStatus;
import com.teachub.promotion.enums.UserCouponStatus;
import com.teachub.promotion.mapper.CouponMapper;
import com.teachub.promotion.mapper.UserCouponMapper;
import com.teachub.promotion.service.IUserCouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * <p>
 * 用户领取优惠券的记录，是真正使用的优惠券信息 服务实现类
 * </p>
 *
 * @author Aeroeia
 * @since 2025-08-01
 */
@Service
@RequiredArgsConstructor
public class UserCouponServiceImpl extends ServiceImpl<UserCouponMapper, UserCoupon> implements IUserCouponService {

}
