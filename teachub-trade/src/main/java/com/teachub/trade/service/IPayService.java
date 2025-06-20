package com.teachub.trade.service;

import com.teachub.trade.domain.dto.OrderDelayQueryDTO;
import com.teachub.trade.domain.dto.PayApplyFormDTO;
import com.teachub.trade.domain.vo.PayChannelVO;

import java.util.List;

public interface IPayService {
    List<PayChannelVO> queryPayChannels();

    String applyPayOrder(PayApplyFormDTO payApply);

    void queryPayResult(OrderDelayQueryDTO message);
}
