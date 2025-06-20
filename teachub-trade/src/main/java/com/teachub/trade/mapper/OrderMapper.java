package com.teachub.trade.mapper;

import com.teachub.trade.domain.po.Order;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 订单 Mapper 接口
 * </p>
 *
 *
 * @  08-29
 */
public interface OrderMapper extends BaseMapper<Order> {

    Order getById(Long id);
}
