package com.teachub.promotion.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teachub.common.domain.dto.PageDTO;
import com.teachub.common.exceptions.BadRequestException;
import com.teachub.common.utils.BeanUtils;
import com.teachub.common.utils.CollUtils;
import com.teachub.common.utils.StringUtils;
import com.teachub.promotion.domain.dto.CouponFormDTO;
import com.teachub.promotion.domain.dto.CouponQuery;
import com.teachub.promotion.domain.po.Coupon;
import com.teachub.promotion.domain.po.CouponScope;
import com.teachub.promotion.domain.vo.CouponPageVO;
import com.teachub.promotion.mapper.CouponMapper;
import com.teachub.promotion.service.ICouponScopeService;
import com.teachub.promotion.service.ICouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        if(CollUtils.isEmpty(records)){
            return PageDTO.empty(page);
        }
        List<CouponPageVO> couponPageVOS = BeanUtils.copyList(records, CouponPageVO.class);
        return PageDTO.of(page,couponPageVOS);
    }
}
