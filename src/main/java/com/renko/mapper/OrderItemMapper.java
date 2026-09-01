package com.renko.mapper;

import com.renko.entities.OrderItemEntity;
import com.renko.payload.dto.OrderItemDto;

public class OrderItemMapper
{
    public static OrderItemDto toDto(OrderItemEntity orderItemEntity)
    {
        return OrderItemDto.builder()
                .id(orderItemEntity.getId())
                .quantity(orderItemEntity.getQuantity())
                .price(orderItemEntity.getPrice())
                .originalPrice(orderItemEntity.getOriginalPrice())
                .discountApplied(orderItemEntity.getDiscountApplied())
                .productDto(ProductMapper.toDto(orderItemEntity.getProductEntity()))
                .productId(orderItemEntity.getProductEntity().getId())
                .orderId(orderItemEntity.getOrderEntity().getId())
                .build();
    }
}
