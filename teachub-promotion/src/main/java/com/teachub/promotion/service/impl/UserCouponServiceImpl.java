package com.teachub.promotion.service.impl;

import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.db.sql.Order;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teachub.common.autoconfigure.mq.RabbitMqHelper;
import com.teachub.common.constants.MqConstants;
import com.teachub.common.domain.dto.PageDTO;
import com.teachub.common.exceptions.BadRequestException;
import com.teachub.common.exceptions.BizIllegalException;
import com.teachub.common.utils.BeanUtils;
import com.teachub.common.utils.CollUtils;
import com.teachub.common.utils.StringUtils;
import com.teachub.common.utils.UserContext;
import com.teachub.promotion.annotation.MyLock;
import com.teachub.promotion.constants.PromotionConstants;
import com.teachub.promotion.discount.Discount;
import com.teachub.promotion.discount.DiscountStrategy;
import com.teachub.promotion.domain.dto.CouponDiscountDTO;
import com.teachub.promotion.domain.dto.CouponQuery;
import com.teachub.promotion.domain.dto.OrderCourseDTO;
import com.teachub.promotion.domain.dto.UserCouponDTO;
import com.teachub.promotion.domain.po.Coupon;
import com.teachub.promotion.domain.po.CouponScope;
import com.teachub.promotion.domain.po.ExchangeCode;
import com.teachub.promotion.domain.po.UserCoupon;
import com.teachub.promotion.domain.vo.CouponVO;
import com.teachub.promotion.enums.DiscountType;
import com.teachub.promotion.enums.ExchangeCodeStatus;
import com.teachub.promotion.enums.MyLockType;
import com.teachub.promotion.enums.UserCouponStatus;
import com.teachub.promotion.mapper.CouponMapper;
import com.teachub.promotion.mapper.UserCouponMapper;
import com.teachub.promotion.service.ICouponScopeService;
import com.teachub.promotion.service.IExchangeCodeService;
import com.teachub.promotion.service.IUserCouponService;
import com.teachub.promotion.utils.CodeUtil;
import com.teachub.promotion.utils.PermuteUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
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
@Slf4j
public class UserCouponServiceImpl extends ServiceImpl<UserCouponMapper, UserCoupon> implements IUserCouponService {
    private final CouponMapper couponMapper;
    private final IExchangeCodeService exchangeCodeService;
    private final StringRedisTemplate redisTemplate;
    private final RabbitMqHelper rabbitMqHelper;
    private final ICouponScopeService couponScopeService;

    @Override
    @MyLock(lockType = MyLockType.RE_ENTRANT_LOCK, key = "lock:coupon:id:#{id}")
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
        if (dto.getSerialNum() != null) {
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

    //查询优惠券方案
    @Override
    public List<CouponDiscountDTO> findDiscountSolution(List<OrderCourseDTO> courseDTOS) {
        //查询我的可用优惠券
        Long userId = UserContext.getUser();
        List<Coupon> coupons = this.getBaseMapper().queryMyCoupon(userId);
        for (Coupon coupon : coupons) {
            log.debug("我的优惠券信息:rule:{}",
                    DiscountStrategy.getDiscount(coupon.getDiscountType()).getRule(coupon));
        }
        //初筛 计算订单总金额是否达到门槛
        int totalNum = courseDTOS.stream()
                .map(OrderCourseDTO::getPrice)
                .reduce(0, Integer::sum);
        List<Coupon> availableCoupons = coupons.stream().filter(coupon -> {
            DiscountType discountType = coupon.getDiscountType();
            return DiscountStrategy.getDiscount(discountType).canUse(totalNum, coupon);
        }).collect(Collectors.toList());
        if (CollUtils.isEmpty(availableCoupons)) {
            return List.of();
        }
        log.debug("初筛后剩余:{}张", availableCoupons.size());
        //细筛 考虑优惠券使用范围
        Map<Coupon, List<OrderCourseDTO>> map = findAvailableCoupons(courseDTOS, availableCoupons);
        log.debug("细筛后结果map:{}", map);
        log.debug("细筛后数量:{}", map.size());
        //排列组合计算最大优惠
        Set<Coupon> avaCoupons = map.keySet();
        List<Coupon> list = new ArrayList<>(avaCoupons);
        List<List<Coupon>> permute = PermuteUtil.permute(list);
        //将单券加入组合
        avaCoupons.forEach(c -> permute.add(List.of(c)));
        log.debug("排列组合数:{}", permute.size());
        //计算每种优惠
        log.debug("开始计算每一种优惠");
        List<CouponDiscountDTO> discountDTOList = new ArrayList<>();
        for (List<Coupon> solution : permute) {
            CouponDiscountDTO dto = calculateSolution(solution, map, courseDTOS);
            log.debug("优惠券id:{},rules:{},最终优惠:{}", dto.getIds(), dto.getRules(), dto.getDiscountAmount());
            discountDTOList.add(dto);
        }
        return discountDTOList;
    }

    /**
     * 计算每个方案的优惠明细
     *
     * @param solution   优惠方案
     * @param map        优惠券和课程可用映射集合
     * @param courseDTOS 订单中课程
     * @return 优惠方案
     */
    private CouponDiscountDTO calculateSolution(List<Coupon> solution, Map<Coupon, List<OrderCourseDTO>> map, List<OrderCourseDTO> courseDTOS) {
        //映射出课程id->优惠价集合
        Map<Long, Integer> courseDiscount = courseDTOS.stream().collect(Collectors.toMap(OrderCourseDTO::getId, u -> 0));
        //遍历优惠券
        CouponDiscountDTO result = new CouponDiscountDTO();
        for (Coupon coupon : solution) {
            //获取优惠券下可用课程集合
            List<OrderCourseDTO> orderCourseDTOS = map.get(coupon);
            //计算当前价格看是否符合优惠要求
            int sum = orderCourseDTOS.stream().mapToInt(u -> u.getPrice() - courseDiscount.get(u.getId())).sum();
            Discount discount = DiscountStrategy.getDiscount(coupon.getDiscountType());
            if (!discount.canUse(sum, coupon)) {
                continue;
            }
            int discountAmount = discount.calculateDiscount(sum, coupon);
            //计算商品优惠后明细
            calculateDiscountDetails(courseDiscount, discountAmount, orderCourseDTOS, sum);
            result.setDiscountAmount(discountAmount + result.getDiscountAmount());
            result.getIds().add(coupon.getId());
            result.getRules().add(discount.getRule(coupon));
        }
        return result;
    }

    /**
     * 计算商品折扣明细
     *
     * @param courseDiscount  商品id->优惠映射
     * @param discountAmount  优惠总价
     * @param orderCourseDTOS 优惠券可用课程集合
     */
    private void calculateDiscountDetails(Map<Long, Integer> courseDiscount, int discountAmount, List<OrderCourseDTO> orderCourseDTOS, int totalAmount) {
        //记录是第几个课程
        int times = 0;
        int remain = discountAmount;
        for (OrderCourseDTO courseDTO : orderCourseDTOS) {
            times++;
            //为防止精度丢失 最后一个计算优惠价课程补全前面丢失的金额
            if (times == orderCourseDTOS.size()) {
                courseDiscount.merge(courseDTO.getId(), remain, Integer::sum);
            } else {
                int discount = courseDTO.getPrice() * discountAmount / totalAmount;
                remain-=discount;
                courseDiscount.merge(courseDTO.getId(), discount, Integer::sum);
            }
        }
    }

    /**
     * 筛选可用优惠券
     *
     * @param courseDTOS       订单中课程集合
     * @param availableCoupons 初筛后可用优惠券
     * @return Map<优惠券, 可用课程集合>
     */
    private Map<Coupon, List<OrderCourseDTO>> findAvailableCoupons(List<OrderCourseDTO> courseDTOS, List<Coupon> availableCoupons) {
        //查找优惠券限定范围
        Map<Coupon, Set<Long>> map = availableCoupons.stream().collect(Collectors.toMap(u -> u, u -> {
            if (!u.getSpecific()) {
                return Set.of();
            }
            List<CouponScope> list = couponScopeService.lambdaQuery()
                    .eq(CouponScope::getCouponId, u.getId())
                    .list();
            return list.stream().map(CouponScope::getBizId).collect(Collectors.toSet());
        }));
        //判断传入的课程是否在优惠券使用范围内
        Map<Coupon, List<OrderCourseDTO>> result = new HashMap<>();
        for (var entry : map.entrySet()) {
            //课程在限定范围内并且金额符合
            Set<Long> value = entry.getValue();
            List<OrderCourseDTO> collect = courseDTOS.stream().filter(u -> {
                if (CollUtils.isEmpty(value)) {
                    return true;
                }
                return value.contains(u.getCateId());
            }).collect(Collectors.toList());
            if (CollUtils.isEmpty(collect)) {
                continue;
            }
            Integer sum = collect.stream().map(OrderCourseDTO::getPrice).reduce(0, Integer::sum);
            Coupon coupon = entry.getKey();
            boolean flag = DiscountStrategy.getDiscount(coupon.getDiscountType()).canUse(sum, coupon);
            if (flag) {
                result.put(coupon, collect);
            }
        }
        return result;
    }
}
