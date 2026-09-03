package com.renko.mapper;

import com.renko.entities.InventoryEntity;
import com.renko.entities.ProductEntity;
import com.renko.entities.StoreEntity;
import com.renko.payload.dto.InventoryDto;

public class InventoryMapper
{
    public static InventoryDto toDto(InventoryEntity inventoryEntity)
    {
        return InventoryDto.builder()
                .id(inventoryEntity.getId())
                .productId(inventoryEntity.getProductEntity() != null ? inventoryEntity.getProductEntity().getId() : null)
                .quantity(inventoryEntity.getQuantity())
                .storeId(inventoryEntity.getStoreEntity() != null ? inventoryEntity.getStoreEntity().getId() : null)
                .lastUpdated(inventoryEntity.getLastUpdated())
                .lowStockThreshold(inventoryEntity.getLowStockThreshold())
                .build();
    }

    public static InventoryEntity toEntity(InventoryDto inventoryDto, StoreEntity storeEntity, ProductEntity productEntity)
    {
        return InventoryEntity.builder()
                .id(inventoryDto.getId())
                .productEntity(productEntity)
                .storeEntity(storeEntity)
                .quantity(inventoryDto.getQuantity())
                .lowStockThreshold(inventoryDto.getLowStockThreshold())
                .lastUpdated(inventoryDto.getLastUpdated())
                .build();
    }
}
