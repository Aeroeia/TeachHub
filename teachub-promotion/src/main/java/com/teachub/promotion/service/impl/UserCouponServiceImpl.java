package com.teachub.promotion.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teachub.common.exceptions.BadRequestException;
import com.teachub.common.exceptions.BizIllegalException;
import com.teachub.common.utils.StringUtils;
import com.teachub.common.utils.UserContext;
import com.teachub.promotion.domain.po.Coupon;
import com.teachub.promotion.domain.po.ExchangeCode;
import com.teachub.promotion.domain.po.UserCoupon;
import com.teachub.promotion.enums.CouponStatus;
import com.teachub.promotion.enums.ExchangeCodeStatus;
import com.teachub.promotion.enums.UserCouponStatus;
import com.teachub.promotion.mapper.CouponMapper;
import com.teachub.promotion.mapper.UserCouponMapper;
import com.teachub.promotion.service.IExchangeCodeService;
import com.teachub.promotion.service.IUserCouponService;
import com.teachub.promotion.utils.CodeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
    private final IExchangeCodeService exchangeCodeService;
    @Autowired
    @Lazy
    private UserCouponServiceImpl userCouponService;

    @Override
    public void receiveCoupon(Long id) {
        Long userId = UserContext.getUser();
        //悲观锁
        //Long比对会有问题 转为String调用intern强调从常量池中取对象
        synchronized (userId.toString().intern()) {
            userCouponService.receiveCopy(id);
        }
    }

    //防止锁失效
    @Transactional
    public void receiveCopy(Long id) {
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new BadRequestException("请先登录");
        }
        //查找优惠券
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null) {
            throw new BizIllegalException("优惠券不存在");
        }
        //判断优惠券状态
        if (!coupon.getStatus().equals(CouponStatus.ISSUING)) {
            throw new BizIllegalException("优惠券不在发放中");
        }
        //判断优惠券领取时间时期
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getIssueBeginTime()) || now.isAfter(coupon.getIssueEndTime())) {
            throw new BizIllegalException("优惠券不在发放时间");
        }
        //判断用户领取数量
        Integer count = this.lambdaQuery()
                .eq(UserCoupon::getCouponId, id)
                .eq(UserCoupon::getUserId, userId)
                .count();
        if (count != null && count >= coupon.getUserLimit()) {
            throw new BizIllegalException("用户已超出领取上限");
        }
        //乐观锁
        couponMapper.updateIssueNum(coupon.getId());
        //新增用户优惠券
        this.saveCoupon(coupon);
    }

    //兑换码兑换优惠券
    @Transactional
    @Override
    public void exchangeCode(String code) {
        if (StringUtils.isBlank(code)) {
            throw new BadRequestException("非法参数");
        }
        //解码
        long serialNum = CodeUtil.parseCode(code);
        //查询redis是否兑换过
        boolean flag = exchangeCodeService.updateExchangeCode(serialNum, true);
        if (flag) {
            throw new BizIllegalException("该优惠券已兑换");
        }
        //抛出异常时对redis进行回滚
        try {
            ExchangeCode exchangeCode = exchangeCodeService.getById(serialNum);
            if (exchangeCode == null) {
                throw new BizIllegalException("兑换码不存在");
            }
            //调用方法
            this.receiveCoupon(exchangeCode.getExchangeTargetId());
            exchangeCodeService.lambdaUpdate()
                    .eq(ExchangeCode::getId, serialNum)
                    .set(ExchangeCode::getStatus, ExchangeCodeStatus.USED)
                    .set(ExchangeCode::getUserId, UserContext.getUser())
                    .update();
        } catch (Exception e) {
            log.error("用户领取优惠券失败", e);
            exchangeCodeService.updateExchangeCode(serialNum, false);
        }
    }

    private void saveCoupon(Coupon coupon) {
        Long userId = UserContext.getUser();
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setCouponId(coupon.getId())
                .setStatus(UserCouponStatus.UNUSED)
                .setUserId(userId);
        if (coupon.getTermBeginTime() == null && coupon.getTermEndTime() == null) {
            LocalDateTime now = LocalDateTime.now();
            userCoupon.setTermBeginTime(now);
            userCoupon.setTermEndTime(now.plusDays(coupon.getTermDays()));
        } else {
            userCoupon.setTermBeginTime(coupon.getTermBeginTime());
            userCoupon.setTermEndTime(coupon.getTermEndTime());
        }
        this.save(userCoupon);
    }
}
