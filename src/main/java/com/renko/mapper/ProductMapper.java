package com.renko.mapper;

import com.renko.entities.ProductEntity;
import com.renko.entities.StoreEntity;
import com.renko.payload.dto.ProductDto;

public class ProductMapper
{
    public static ProductDto toDto(ProductEntity productEntity)
    {
        ProductDto productDto = new ProductDto();
        productDto.setFromEntity(productEntity);
        return productDto;
    }

    public static ProductEntity toEntity(ProductDto productDto, StoreEntity storeEntity)
    {
        ProductEntity productEntity = new ProductEntity();
        productEntity.setFromDto(productDto, storeEntity);
        return productEntity;
    }
}
