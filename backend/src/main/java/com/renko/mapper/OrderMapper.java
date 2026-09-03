package com.renko.mapper;

import com.renko.entities.OrderEntity;
import com.renko.entities.OrderItemEntity;
import com.renko.entities.UserEntity;
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

        List<OrderItemDto> itemsFromEntity = orderEntity.getItems()
                .stream()
                .filter(Objects::nonNull)
                .map(OrderItemMapper::toDto)
                .collect(Collectors.toList());

        return OrderDto.builder()
                .id(orderEntity.getId())
                .totalAmount(orderEntity.getTotalAmount())
                .subtotal(orderEntity.getSubtotal())
                .totalDiscount(orderEntity.getTotalDiscount())
                .createdAt(orderEntity.getCreatedAt())
                .updatedAt(orderEntity.getUpdatedAt())
                .storeId(orderEntity.getStoreEntity().getId())
                .customerId(orderEntity.getCustomerEntity().getId())
                .customerName(orderEntity.getCustomerEntity().getFullName())
                .customerPhone(orderEntity.getCustomerEntity().getPhone())
                .cashier(UserMapper.toDto(orderEntity.getCashierEntity()))
                .stripePaymentIntentId(orderEntity.getStripePaymentIntentId())
                .items(itemsFromEntity)
                .build();
    }
}
