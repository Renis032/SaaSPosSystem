package com.renko.service;

import com.renko.entities.StoreEntity;
import com.renko.exceptions.UserException;
import com.renko.payload.dto.CategoryDto;

import java.util.List;

public interface CategoryService
{
    CategoryDto createCategoryDto(CategoryDto categoryDto) throws Exception;
    List<CategoryDto> getCategoriesByStore(Long storeId);
    CategoryDto updateCategory(Long id, CategoryDto categoryDto) throws Exception;
    void deleteCategory(Long id) throws Exception;
}
