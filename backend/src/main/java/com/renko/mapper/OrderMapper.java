package com.renko.mapper;

import com.renko.entities.OrderEntity;
import com.renko.payload.dto.OrderDto;
import com.renko.payload.dto.OrderItemDto;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class OrderMapper
{
    public static OrderDto toDto(OrderEntity orderEntity)
    {
//        List<OrderItemEntity> itemsFromEntity = orderEntity.getItems() != null ? orderEntity.getItems()
//                                                                                  .stream()
//                                                                                  .filter(Objects::nonNull)
//                                                                                  .map(OrderItemMapper::toDto)
//                                                                                  .collect(Collectors.toList())
//                : Collections.emptyList();

        List<OrderItemDto> itemsFromEntity = orderEntity.getItems() == null
                ? Collections.emptyList()
                : orderEntity.getItems()
                        .stream()
                        .filter(Objects::nonNull)
                        .map(item -> OrderItemMapper.toDto(item, orderEntity.getId()))
                        .collect(Collectors.toList());

        return OrderDto.builder()
                .id(orderEntity.getId())
                .totalAmount(orderEntity.getTotalAmount())
                .subtotal(orderEntity.getSubtotal())
                .totalDiscount(orderEntity.getTotalDiscount())
                .createdAt(orderEntity.getCreatedAt())
                .updatedAt(orderEntity.getUpdatedAt())
                .storeId(orderEntity.getStoreEntity() != null ? orderEntity.getStoreEntity().getId() : null)
                .customerId(orderEntity.getCustomerEntity() != null ? orderEntity.getCustomerEntity().getId() : null)
                .customerName(orderEntity.getCustomerEntity() != null ? orderEntity.getCustomerEntity().getFullName() : null)
                .customerPhone(orderEntity.getCustomerEntity() != null ? orderEntity.getCustomerEntity().getPhone() : null)
                .cashierId(orderEntity.getCashierEntity() != null ? orderEntity.getCashierEntity().getId() : null)
                .paymentType(orderEntity.getPaymentType())
                .stripePaymentIntentId(orderEntity.getStripePaymentIntentId())
                .items(itemsFromEntity)
                .build();
    }
}
