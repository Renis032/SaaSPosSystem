package com.renko.service;

import com.renko.domain.OrderStatus;
import com.renko.domain.PaymentType;
import com.renko.payload.dto.OrderDto;
import com.renko.payload.dto.ReceiptDto;

import java.util.List;

public interface OrderService
{
    OrderDto createOrder(OrderDto orderDto) throws Exception;
    OrderDto updateOrder(Long id, OrderDto orderDto) throws Exception;
    OrderDto getOrderById(Long id);

    List<OrderDto> getOrdersByStore(Long storeId,
                                    Long customerId,
                                    Long cashierId,
                                    PaymentType paymentType,
                                    OrderStatus orderStatus);

    List<OrderDto> getOrdersByCashier(Long cashierId);

    void deleteOrder(Long id);

    List<OrderDto> getOrdersByCustomerId(Long customerId);

    List<OrderDto> getTodayOrdersByStore(Long storeId);

    List<OrderDto> getTop5RecentOrdersByStoreId(Long storeId);

    ReceiptDto getReceipt(Long orderId);
}
