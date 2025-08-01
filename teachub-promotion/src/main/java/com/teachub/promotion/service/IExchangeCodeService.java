package com.teachub.promotion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teachub.common.domain.dto.PageDTO;
import com.teachub.promotion.domain.dto.CodeQuery;
import com.teachub.promotion.domain.po.Coupon;
import com.teachub.promotion.domain.po.ExchangeCode;
import com.teachub.promotion.domain.vo.CodeVO;

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

    PageDTO<CodeVO> queryCodesPage(CodeQuery query);
}
