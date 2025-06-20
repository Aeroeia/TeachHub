package com.teachub.trade.mapper;

import com.teachub.trade.domain.po.RefundApply;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 退款申请 Mapper 接口
 * </p>
 *
 *   
 * @  08-29
 */
public interface RefundApplyMapper extends BaseMapper<RefundApply> {

    @Select("SELECT id FROM refund_apply WHERE status = 1 LIMIT 1")
    Long nextRefundApplyToApprove();

    List<RefundApply> queryByDetailId(Long detailId);
}
