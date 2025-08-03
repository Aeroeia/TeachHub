package com.teachub.promotion.service.impl;

import cn.hutool.core.bean.copier.CopyOptions;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teachub.common.autoconfigure.mq.RabbitMqHelper;
import com.teachub.common.constants.MqConstants;
import com.teachub.common.domain.dto.PageDTO;
import com.teachub.common.exceptions.BadRequestException;
import com.teachub.common.exceptions.BizIllegalException;
import com.teachub.common.utils.BeanUtils;
import com.teachub.common.utils.CollUtils;
import com.teachub.common.utils.UserContext;
import com.teachub.promotion.annotation.MyLock;
import com.teachub.promotion.constants.PromotionConstants;
import com.teachub.promotion.domain.dto.CouponQuery;
import com.teachub.promotion.domain.dto.UserCouponDTO;
import com.teachub.promotion.domain.po.Coupon;
import com.teachub.promotion.domain.po.ExchangeCode;
import com.teachub.promotion.domain.po.UserCoupon;
import com.teachub.promotion.domain.vo.CouponVO;
import com.teachub.promotion.enums.ExchangeCodeStatus;
import com.teachub.promotion.enums.MyLockType;
import com.teachub.promotion.enums.UserCouponStatus;
import com.teachub.promotion.mapper.CouponMapper;
import com.teachub.promotion.mapper.UserCouponMapper;
import com.teachub.promotion.service.IExchangeCodeService;
import com.teachub.promotion.service.IUserCouponService;
import com.teachub.promotion.utils.CodeUtil;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final StringRedisTemplate redisTemplate;
    private final RabbitMqHelper rabbitMqHelper;

    @Override
    @MyLock(lockType = MyLockType.RE_ENTRANT_LOCK,key = "lock:coupon:id:#{id}")
    public void receiveCoupon(Long id) {
        //从aop上下文获取代理对象
        IUserCouponService iUserCouponService = (IUserCouponService) AopContext.currentProxy();
        iUserCouponService.receiveCopy(id);
    }

    /*防止锁失效
    @Transactional
     */
    public void receiveCopy(Long id) {
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new BadRequestException("请先登录");
        }
        //查找优惠券
        String key = PromotionConstants.COUPON_CACHE_KEY_PREFIX + id;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        Coupon coupon = BeanUtils.mapToBean(entries, Coupon.class, false, CopyOptions.create());
        if (coupon == null) {
            throw new BizIllegalException("优惠券不存在或未发放");
        }
        /*
        判断优惠券状态 只要存入Redis一定在发放中
        if (!coupon.getStatus().equals(CouponStatus.ISSUING)) {
            throw new BizIllegalException("优惠券不在发放中");
        }
        */

        //判断优惠券领取时间时期
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getIssueBeginTime()) || now.isAfter(coupon.getIssueEndTime())) {
            throw new BizIllegalException("优惠券不在发放时间");
        }
        //判断优惠券是否领完
        if (coupon.getTotalNum() <= 0) {
            throw new BizIllegalException("优惠券已领完");
        }
        //判断用户领取数量 通过redis
        String userKey = PromotionConstants.USER_COUPON_CACHE_KEY_PREFIX + id;
        Long increment = redisTemplate.opsForHash().increment(userKey, userId.toString(), 1);
        if (increment > coupon.getUserLimit()) {
            throw new BizIllegalException("用户已超出领取上限");
        }
        //扣减库存
        redisTemplate.opsForHash().increment(key, "totalNum", -1);
        /*
        //乐观锁
        int i = couponMapper.updateIssueNum(coupon.getId());
        if (i == 0) {
            throw new BizIllegalException("货存不足");
        }
        //新增用户优惠券
        this.saveCoupon(coupon);
        */
        //发送mq新增db记录
        UserCouponDTO userCouponDTO = new UserCouponDTO();
        userCouponDTO.setCouponId(id);
        userCouponDTO.setUserId(userId);
        rabbitMqHelper.send(MqConstants.Exchange.PROMOTION_EXCHANGE,
                MqConstants.Key.COUPON_RECEIVE,
                userCouponDTO);
    }


    //兑换码兑换优惠券
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

        } catch (Exception e) {
            log.error("用户领取优惠券失败", e);
            exchangeCodeService.updateExchangeCode(serialNum, false);
            throw new BizIllegalException("用户领取优惠券失败");
        }
    }

    @Override
    public void saveCoupon(UserCouponDTO dto) {
        Long userId = dto.getUserId();
        //根据id查找优惠券
        Coupon coupon = couponMapper.selectById(dto.getCouponId());
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
        if(dto.getSerialNum()!=null){
            exchangeCodeService.lambdaUpdate()
                    .eq(ExchangeCode::getId, dto.getSerialNum())
                    .set(ExchangeCode::getStatus, ExchangeCodeStatus.USED)
                    .set(ExchangeCode::getUserId, userId)
                    .update();
        }
    }

    //查询我的优惠券
    @Override
    public PageDTO<CouponVO> queryMyCoupons(CouponQuery couponQuery) {
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new BadRequestException("用户未登陆");
        }
        Page<UserCoupon> page = this.lambdaQuery()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getStatus, couponQuery.getStatus())
                .page(couponQuery.toMpPageDefaultSortByCreateTimeDesc());
        List<UserCoupon> records = page.getRecords();
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(page);
        }
        Set<Long> collect = records.stream().map(UserCoupon::getCouponId).collect(Collectors.toSet());
        List<Coupon> coupons = couponMapper.selectBatchIds(collect);
        Map<Long, Coupon> map = coupons.stream().collect(Collectors.toMap(Coupon::getId, u -> u));
        List<CouponVO> result = records.stream().map(po -> {
            Long couponId = po.getCouponId();
            Coupon coupon = map.get(couponId);
            CouponVO couponVO = BeanUtils.copyBean(coupon, CouponVO.class);
            couponVO.setTermEndTime(po.getTermEndTime());
            return couponVO;
        }).collect(Collectors.toList());
        return PageDTO.of(page, result);
    }
}
