package com.renko.payload.dto.updates;

import com.renko.payload.dto.ProductDto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class InventoryUpdateDto
{
    private ProductDto productDto;
    private Integer quantity;

    private Long storeId;
    private Long productId;

    private Integer lowStockThreshold;

    private LocalDateTime lastUpdated;
}
