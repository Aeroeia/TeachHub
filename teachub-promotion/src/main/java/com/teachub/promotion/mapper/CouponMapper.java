package com.teachub.promotion.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teachub.promotion.domain.po.Coupon;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 优惠券的规则信息 Mapper 接口
 * </p>
 *
 * @author Aeroeia
 * @since 2025-07-30
 */
public interface CouponMapper extends BaseMapper<Coupon> {
    @Update("update coupon set issue_num = issue_num+1 where id = #{id} and issue_num < total_num")
    void updateIssueNum(Long id);
}
