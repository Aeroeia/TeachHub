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
    private final CouponMapper couponMapper;
    @Override
    @Transactional
    public void receiveCoupon(Long id) {
        Long userId = UserContext.getUser();
        if(userId==null){
            throw new BadRequestException("请先登录");
        }
        //查找优惠券
        Coupon coupon = couponMapper.selectById(id);
        if(coupon==null){
            throw new BizIllegalException("优惠券不存在");
        }
        //判断优惠券状态
        if(!coupon.getStatus().equals(CouponStatus.ISSUING)){
            throw new BizIllegalException("优惠券不在发放中");
        }
        //判断优惠券领取时间时期
        LocalDateTime now = LocalDateTime.now();
        if(now.isBefore(coupon.getIssueBeginTime())||now.isAfter(coupon.getIssueEndTime())){
            throw new BizIllegalException("优惠券不在发放时间");
        }
        //判断用户领取数量
        Integer count = this.lambdaQuery()
                .eq(UserCoupon::getCouponId, id)
                .eq(UserCoupon::getUserId, userId)
                .count();
        if(count!=null&&count>=coupon.getUserLimit()){
            throw new BizIllegalException("用户已超出领取上限");
        }
        //乐观锁
        couponMapper.updateIssueNum(coupon.getId());
        //新增用户优惠券
        this.saveCoupon(coupon);
    }

    private void saveCoupon(Coupon coupon) {
        Long userId = UserContext.getUser();
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setCouponId(coupon.getId())
                .setStatus(UserCouponStatus.UNUSED)
                .setUserId(userId);
        if(coupon.getTermBeginTime()==null&&coupon.getTermEndTime()==null){
            LocalDateTime now = LocalDateTime.now();
            userCoupon.setTermBeginTime(now);
            userCoupon.setTermEndTime(now.plusDays(coupon.getTermDays()));
        }
        else{
            userCoupon.setTermBeginTime(coupon.getTermBeginTime());
            userCoupon.setTermEndTime(coupon.getTermEndTime());
        }
        this.save(userCoupon);
    }
}
