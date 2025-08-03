package com.teachub.promotion.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teachub.api.client.course.CategoryClient;
import com.teachub.api.dto.course.CategoryBasicDTO;
import com.teachub.common.domain.dto.PageDTO;
import com.teachub.common.exceptions.BadRequestException;
import com.teachub.common.exceptions.BizIllegalException;
import com.teachub.common.utils.BeanUtils;
import com.teachub.common.utils.CollUtils;
import com.teachub.common.utils.DateUtils;
import com.teachub.common.utils.UserContext;
import com.teachub.promotion.constants.PromotionConstants;
import com.teachub.promotion.domain.dto.CouponFormDTO;
import com.teachub.promotion.domain.dto.CouponIssueFormDTO;
import com.teachub.promotion.domain.dto.CouponQuery;
import com.teachub.promotion.domain.po.Coupon;
import com.teachub.promotion.domain.po.CouponScope;
import com.teachub.promotion.domain.po.ExchangeCode;
import com.teachub.promotion.domain.po.UserCoupon;
import com.teachub.promotion.domain.vo.CouponDetailVO;
import com.teachub.promotion.domain.vo.CouponPageVO;
import com.teachub.promotion.domain.vo.CouponScopeVO;
import com.teachub.promotion.domain.vo.CouponVO;
import com.teachub.promotion.enums.CouponStatus;
import com.teachub.promotion.enums.ObtainType;
import com.teachub.promotion.enums.UserCouponStatus;
import com.teachub.promotion.mapper.CouponMapper;
import com.teachub.promotion.service.ICouponScopeService;
import com.teachub.promotion.service.ICouponService;
import com.teachub.promotion.service.IExchangeCodeService;
import com.teachub.promotion.service.IUserCouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 优惠券的规则信息 服务实现类
 * </p>
 *
 * @author Aeroeia
 * @since 2025-07-30
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements ICouponService {
    private final ICouponScopeService couponScopeService;
    private final IExchangeCodeService codeService;
    private final CategoryClient categoryClient;
    private final IUserCouponService userCouponService;
    private final StringRedisTemplate redisTemplate;

    //新增优惠券
    @Transactional
    @Override
    public void addCoupon(CouponFormDTO coupon) {
        //转po
        Coupon po = BeanUtils.copyBean(coupon, Coupon.class);
        this.save(po);
        //判断是否有限定范围
        if (coupon.getSpecific() == null || !coupon.getSpecific()) {
            return;
        }
        //获取范围
        List<Long> scopes = coupon.getScopes();
        if (CollUtils.isEmpty(scopes)) {
            throw new BadRequestException("限定范围为空");
        }

        List<CouponScope> list = scopes.stream().map(id -> {
            CouponScope couponScope = new CouponScope();
            couponScope.setCouponId(po.getId());
            couponScope.setBizId(id);
            //目前type固定为1
            couponScope.setType(1);
            return couponScope;
        }).collect(Collectors.toList());
        //新增优惠券范围
        couponScopeService.saveBatch(list);
    }

    //分页查询优惠券
    @Override
    public PageDTO<CouponPageVO> queryCouponPage(CouponQuery query) {
        Page<Coupon> page = this.lambdaQuery()
                .eq(query.getType() != null, Coupon::getType, query.getType())
                .eq(query.getStatus() != null, Coupon::getStatus, query.getStatus())
                .like(StringUtils.isNotBlank(query.getName()), Coupon::getName, query.getName())
                .page(query.toMpPageDefaultSortByCreateTimeDesc());
        List<Coupon> records = page.getRecords();
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(page);
        }
        List<CouponPageVO> couponPageVOS = BeanUtils.copyList(records, CouponPageVO.class);
        return PageDTO.of(page, couponPageVOS);
    }

    //发放优惠券
    @Override
    public void issueCoupon(Long id, CouponIssueFormDTO couponIssueFormDTO) {
        log.info("当前线程名:{}", Thread.currentThread().getName());
        //查询优惠券
        Coupon one = this.lambdaQuery()
                .eq(Coupon::getId, id)
                .one();
        if (one == null) {
            throw new BizIllegalException("优惠券不存在");
        }
        //校验优惠券状态
        if (!one.getStatus().equals(CouponStatus.DRAFT) && one.getStatus().equals(CouponStatus.PAUSE)) {
            throw new BizIllegalException("优惠券状态异常");
        }
        //判断是否立即发放
        LocalDateTime now = LocalDateTime.now();
        boolean isStartIssue = couponIssueFormDTO.getIssueBeginTime() == null || !couponIssueFormDTO.getIssueBeginTime().isAfter(now);
        Coupon coupon = BeanUtils.copyBean(couponIssueFormDTO, Coupon.class);
        if (isStartIssue) {
            coupon.setStatus(CouponStatus.ISSUING);
            coupon.setIssueBeginTime(now);
        } else {
            coupon.setStatus(CouponStatus.UN_ISSUE);
        }
        this.updateById(coupon);
        //如果立即发放 存到redis缓存
        if (isStartIssue) {
            String key = PromotionConstants.COUPON_CACHE_KEY_PREFIX + id;
            Map<String, String> map = new HashMap<>();
            map.put("issueBeginTime", coupon.getIssueBeginTime() == null ? String.valueOf(DateUtils.toEpochMilli(LocalDateTime.now())) : String.valueOf(DateUtils.toEpochMilli(coupon.getIssueBeginTime())));
            map.put("issueEndTime",String.valueOf(DateUtils.toEpochMilli(coupon.getIssueEndTime())));
            map.put("totalNum",one.getTotalNum().toString());
            map.put("userLimit",one.getUserLimit().toString());
            redisTemplate.opsForHash().putAll(key,map);
        }
        // 如果兑换方式是指定发放 要生成兑换码
        if (one.getObtainWay().equals(ObtainType.ISSUE) && one.getStatus().equals(CouponStatus.DRAFT)) {
            one.setIssueEndTime(coupon.getIssueEndTime());
            codeService.asyncCreateCode(one);
        }
    }

    //更新优惠券信息
    @Transactional
    @Override
    public void updateCoupon(CouponFormDTO dto, Long id) {
        Coupon coupon = this.getById(id);
        if (coupon == null) {
            throw new BizIllegalException("优惠券不存在");
        }
        if (coupon.getStatus() == CouponStatus.ISSUING) {
            throw new BizIllegalException("优惠券正在发放中，请勿修改");
        }
        Coupon po = BeanUtils.copyBean(dto, Coupon.class);
        this.updateById(po);
        //是否限定分类
        if (po.getSpecific() != null && po.getSpecific()) {
            List<Long> scopes = dto.getScopes();
            List<CouponScope> collect = scopes.stream().map(scopeId -> {
                CouponScope scope = new CouponScope();
                scope.setCouponId(po.getId())
                        .setType(1)
                        .setBizId(scopeId);
                return scope;
            }).collect(Collectors.toList());
            couponScopeService.saveBatch(collect);
        }
    }

    //查看优惠券详情信息
    @Override
    public CouponDetailVO queryCouponDetail(Long id) {
        Coupon coupon = this.getById(id);
        if (coupon == null) {
            throw new BizIllegalException("优惠券不存在");
        }
        CouponDetailVO couponDetailVO = BeanUtils.copyBean(coupon, CouponDetailVO.class);
        //查看限定的分类信息
        if (coupon.getSpecific() != null && coupon.getSpecific()) {
            List<CouponScope> list = couponScopeService.lambdaQuery()
                    .eq(CouponScope::getCouponId, id)
                    .list();
            List<CategoryBasicDTO> allOfOneLevel = categoryClient.getAllOfOneLevel();
            Map<Long, String> map = allOfOneLevel.stream().collect(Collectors.toMap(CategoryBasicDTO::getId, CategoryBasicDTO::getName));
            List<CouponScopeVO> scopeVOS = list.stream().map((po) -> {
                CouponScopeVO scopeVO = new CouponScopeVO();
                scopeVO.setId(po.getBizId());
                scopeVO.setName(map.get(po.getBizId()));
                return scopeVO;
            }).collect(Collectors.toList());
            couponDetailVO.setScopes(scopeVOS);
        }
        return couponDetailVO;
    }

    //删除优惠券
    @Override
    public void delete(Long id) {
        this.removeById(id);
        couponScopeService.lambdaUpdate()
                .eq(CouponScope::getCouponId, id)
                .remove();
        codeService.lambdaUpdate()
                .eq(ExchangeCode::getExchangeTargetId, id)
                .remove();
    }

    //暂停发放优惠券
    @Override
    public void pauseCoupon(Long id) {
        this.lambdaUpdate()
                .eq(Coupon::getId, id)
                .set(Coupon::getStatus, CouponStatus.PAUSE)
                .update();
    }

    //查询发放中的优惠券
    @Override
    public List<CouponVO> queryList() {
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new BadRequestException("用户登陆状态异常");
        }
        //查询db发放中且为手动领取的优惠券
        List<Coupon> list = this.lambdaQuery().
                eq(Coupon::getStatus, CouponStatus.ISSUING)
                .eq(Coupon::getObtainWay, ObtainType.PUBLIC)
                .list();
        if (CollUtils.isEmpty(list)) {
            return List.of();
        }
        Set<Long> couponIds = list.stream().map(Coupon::getId).collect(Collectors.toSet());
        //查看当前用户所领过的券
        List<UserCoupon> myCoupons = userCouponService.lambdaQuery()
                .eq(UserCoupon::getUserId, userId)
                .in(UserCoupon::getCouponId, couponIds)
                .list();
        //查询优惠券用户用了的次数
        Map<Long, Long> couponMap = myCoupons.stream().
                collect(Collectors.groupingBy(UserCoupon::getCouponId, Collectors.counting()));
        //未使用的券
        Map<Long, Long> unusedCouponMap = myCoupons.stream().
                filter(coupon -> coupon.getStatus() == UserCouponStatus.UNUSED).
                collect(Collectors.groupingBy(UserCoupon::getCouponId, Collectors.counting()));
        //过滤掉超出上限且被领取过的券
        return list.stream().map(po -> {
            CouponVO couponVO = BeanUtils.copyBean(po, CouponVO.class);
            //优惠券是否超出领取上限
            boolean isAvl = po.getIssueNum() < po.getTotalNum() && couponMap.getOrDefault(po.getId(), 0L) < po.getUserLimit();
            couponVO.setAvailable(isAvl);
            boolean isRec = unusedCouponMap.getOrDefault(po.getId(), 0L) > 0;
            couponVO.setReceived(isRec);
            return couponVO;
        }).collect(Collectors.toList());
    }


}
