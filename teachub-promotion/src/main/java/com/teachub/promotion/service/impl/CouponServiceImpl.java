package com.teachub.promotion.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teachub.common.domain.dto.PageDTO;
import com.teachub.common.exceptions.BadRequestException;
import com.teachub.common.exceptions.BizIllegalException;
import com.teachub.common.utils.BeanUtils;
import com.teachub.common.utils.CollUtils;
import com.teachub.common.utils.StringUtils;
import com.teachub.promotion.domain.dto.CouponFormDTO;
import com.teachub.promotion.domain.dto.CouponIssueFormDTO;
import com.teachub.promotion.domain.dto.CouponQuery;
import com.teachub.promotion.domain.po.Coupon;
import com.teachub.promotion.domain.po.CouponScope;
import com.teachub.promotion.domain.vo.CouponPageVO;
import com.teachub.promotion.enums.CouponStatus;
import com.teachub.promotion.enums.ObtainType;
import com.teachub.promotion.mapper.CouponMapper;
import com.teachub.promotion.service.ICouponScopeService;
import com.teachub.promotion.service.ICouponService;
import com.teachub.promotion.service.IExchangeCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements ICouponService {
    private final ICouponScopeService couponScopeService;
    private final IExchangeCodeService codeService;
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

    @Override
    public void issueCoupon(Long id, CouponIssueFormDTO couponIssueFormDTO) {
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
        boolean isStartIssue = couponIssueFormDTO.getIssueBeginTime() == null || !couponIssueFormDTO.getIssueEndTime().isAfter(now);
        Coupon coupon = BeanUtils.copyBean(couponIssueFormDTO, Coupon.class);
        if(isStartIssue){
            coupon.setStatus(CouponStatus.ISSUING);
            coupon.setIssueBeginTime(now);
        }
        else{
            coupon.setStatus(CouponStatus.UN_ISSUE);
        }
        this.updateById(coupon);

        //TODO 如果兑换方式是指定发放 要生成兑换码
        if(one.getObtainWay().equals(ObtainType.ISSUE)&&one.getStatus().equals(CouponStatus.DRAFT)){
            one.setTermEndTime(coupon.getTermEndTime());
            codeService.asyncCreateCode(one);
        }
    }
}
