package com.renko.mapper;

import com.renko.entities.CategoryEntity;
import com.renko.payload.dto.CategoryDto;

public class CategoryMapper
{
    public static CategoryDto toDto(CategoryEntity categoryEntity)
    {
        return CategoryDto.builder()
                .id(categoryEntity.getId())
                .name(categoryEntity.getName())
                .storeId(categoryEntity.getStoreEntity() != null ? categoryEntity.getStoreEntity().getId() : null)
                .build();
    }
}
