package com.teachub.pay.service.dubbo;

import com.teachub.common.exceptions.BadRequestException;
import com.teachub.common.utils.BeanUtils;
import com.teachub.pay.sdk.client.PayClient;
import com.teachub.pay.sdk.constants.PayErrorInfo;
import com.teachub.pay.sdk.constants.PayType;
import com.teachub.pay.sdk.dto.*;
import com.teachub.pay.service.IPayChannelService;
import com.teachub.pay.service.IPayOrderService;
import com.teachub.pay.service.IRefundOrderService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@DubboService
public class PayDubboServiceImpl implements PayClient {

    @Autowired
    private IPayChannelService channelService;
    @Autowired
    private IPayOrderService payOrderService;
    @Autowired
    private IRefundOrderService refundOrderService;

    @Override
    public List<PayChannelDTO> listAllPayChannels() {
        return BeanUtils.copyList(channelService.list(), PayChannelDTO.class);
    }

    @Override
    public String applyPayOrder(PayApplyDTO payApplyDTO) {
        if(!PayType.NATIVE.equalsValue(payApplyDTO.getPayType())){
            throw new BadRequestException(PayErrorInfo.INVALID_PAY_TYPE);
        }
        return payOrderService.applyPayOrder(payApplyDTO);
    }

    @Override
    public PayResultDTO queryPayResult(Long bizOrderId) {
        return payOrderService.queryPayResult(bizOrderId);
    }

    @Override
    public RefundResultDTO applyRefund(RefundApplyDTO refundApplyDTO) {
        return refundOrderService.applyRefund(refundApplyDTO);
    }

    @Override
    public RefundResultDTO queryRefundResult(Long bizRefundOrderId) {
        return refundOrderService.queryRefundResult(bizRefundOrderId);
    }
}
