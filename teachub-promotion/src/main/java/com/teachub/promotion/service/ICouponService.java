package com.teachub.promotion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teachub.common.domain.dto.PageDTO;
import com.teachub.promotion.domain.dto.CouponFormDTO;
import com.teachub.promotion.domain.dto.CouponIssueFormDTO;
import com.teachub.promotion.domain.dto.CouponQuery;
import com.teachub.promotion.domain.po.Coupon;
import com.teachub.promotion.domain.vo.CouponDetailVO;
import com.teachub.promotion.domain.vo.CouponPageVO;

/**
 * <p>
 * 优惠券的规则信息 服务类
 * </p>
 *
 * @author Aeroeia
 * @since 2025-07-30
 */
public interface ICouponService extends IService<Coupon> {

    void addCoupon(CouponFormDTO coupon);

    PageDTO<CouponPageVO> queryCouponPage(CouponQuery query);

    void issueCoupon(Long id, CouponIssueFormDTO couponIssueFormDTO);

    void updateCoupon(CouponFormDTO coupon, Long id);

    CouponDetailVO queryCouponDetail(Long id);

    void delete(Long id);

    void pauseCoupon(Long id);
}
