package com.renko.mapper;

import com.renko.entities.CategoryEntity;
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

    public static ProductEntity toEntity(ProductDto productDto, StoreEntity storeEntity, CategoryEntity categoryEntity)
    {
        ProductEntity productEntity = new ProductEntity();

        productEntity.setId(productDto.getId());
        productEntity.setName(productDto.getName());
        productEntity.setSku(productDto.getSku());
        productEntity.setDescription(productDto.getDescription());
        productEntity.setMaxRetailPrice(productDto.getMaxRetailPrice());
        productEntity.setSellingPrice(productDto.getSellingPrice());
        productEntity.setBrand(productDto.getBrand());
        productEntity.setImageUrl(productDto.getImageUrl());
        productEntity.setCategoryEntity(categoryEntity);
        productEntity.setStoreEntity(storeEntity);
        productEntity.setCreatedAt(productDto.getCreatedAt());
        productEntity.setUpdatedAt(productDto.getUpdatedAt());

        return productEntity;
    }
}
