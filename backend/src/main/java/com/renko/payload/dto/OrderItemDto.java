package com.renko.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto
{
    private Long id;

    private Integer quantity;

    private Double price;
    private Double originalPrice;
    private Double discountApplied;

    private ProductDto productDto;

    private Long productId;

    private Long orderId;
}
