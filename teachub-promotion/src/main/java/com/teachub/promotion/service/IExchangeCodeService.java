package com.teachub.promotion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teachub.promotion.domain.po.Coupon;
import com.teachub.promotion.domain.po.ExchangeCode;

/**
 * <p>
 * 兑换码 服务类
 * </p>
 *
 * @author Aeroeia
 * @since 2025-07-30
 */
public interface IExchangeCodeService extends IService<ExchangeCode> {

    void asyncCreateCode(Coupon one);
}
