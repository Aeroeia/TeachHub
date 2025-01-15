package com.teachub.pay.service;

import com.teachub.common.domain.dto.PageDTO;
import com.teachub.pay.domain.po.RefundOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.teachub.pay.sdk.dto.RefundApplyDTO;
import com.teachub.pay.sdk.dto.RefundResultDTO;

/**
 * <p>
 * 退款订单 服务类
 * </p>
 *
 * 
 * @  08-26
 */
public interface IRefundOrderService extends IService<RefundOrder> {

    RefundResultDTO applyRefund(RefundApplyDTO refundApplyDTO);

    RefundResultDTO queryRefundResult(Long bizRefundOrderId);

    RefundOrder queryByRefundOrderNo(Long refundOrderNo);

    PageDTO<RefundOrder> queryRefundingOrderByPage(int pageNo, int size);

    void checkRefundOrder(RefundOrder refundOrder);

    RefundResultDTO queryRefundDetail(Long bizRefundOrderId);
}
