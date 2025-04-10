package com.teachub.pay.sdk.client;

import com.teachub.pay.sdk.dto.*;
import java.util.List;

public interface PayClient {
    /**
     * 查询支付渠道
     * @return 支付渠道列表
     */
    List<PayChannelDTO> listAllPayChannels();
    /**
     * 扫码支付申请支付单，返回支付url地址，用于生产二维码
     *
     * @param payApplyDTO 申请支付单的参数信息
     * @return 支付链接，需要前端生成二维码
     */
    String applyPayOrder(PayApplyDTO payApplyDTO);

    /**
     * 根据业务端订单id查询支付结果
     *
     * @param bizOrderId 业务订单id
     * @return 支付结果
     */
    PayResultDTO queryPayResult(Long bizOrderId);

    /**
     * 申请退款接口
     *
     * @param refundApplyDTO 退款参数
     * @return 退款结果
     */
    RefundResultDTO applyRefund(RefundApplyDTO refundApplyDTO);

    /**
     * 查询退款结果
     *
     * @param bizRefundOrderId 要退款的订单id
     * @return 退款结果
     */
    RefundResultDTO queryRefundResult(Long bizRefundOrderId);
}
