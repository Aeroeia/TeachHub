package com.teachub.trade.service;

import com.teachub.common.domain.dto.PageDTO;
import com.teachub.pay.sdk.dto.PayResultDTO;
import com.teachub.trade.domain.dto.PlaceOrderDTO;
import com.teachub.trade.domain.po.Order;
import com.baomidou.mybatisplus.extension.service.IService;
import com.teachub.trade.domain.po.OrderDetail;
import com.teachub.trade.domain.query.OrderPageQuery;
import com.teachub.trade.domain.vo.OrderConfirmVO;
import com.teachub.trade.domain.vo.OrderPageVO;
import com.teachub.trade.domain.vo.OrderVO;
import com.teachub.trade.domain.vo.PlaceOrderResultVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 订单 服务类
 * </p>
 *
 *
 * @  08-29
 */
public interface IOrderService extends IService<Order> {

    PlaceOrderResultVO placeOrder(PlaceOrderDTO placeOrderDTO);

    @Transactional
    void saveOrderAndDetails(Order order, List<OrderDetail> orderDetails);

    void cancelOrder(Long orderId);

    void deleteOrder(Long id);

    PageDTO<OrderPageVO> queryMyOrderPage(OrderPageQuery pageQuery);

    OrderVO queryOrderById(Long id);

    PlaceOrderResultVO queryOrderStatus(Long orderId);

    void handlePaySuccess(PayResultDTO payResult);

    PlaceOrderResultVO enrolledFreeCourse(Long courseId);

    OrderConfirmVO prePlaceOrder(List<Long> courseIds);

}
