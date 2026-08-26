package com.renko.service;

import com.renko.entities.UserEntity;
import com.renko.payload.dto.ProductDto;

import java.util.List;

public interface ProductService
{
    ProductDto createProduct(ProductDto productDto, UserEntity userEntity);
    ProductDto updateProduct(Long id, ProductDto productDto, UserEntity userEntity);
    void deleteProduct(Long id, UserEntity userEntity);
    List<ProductDto> getProductsByStoreId(Long storeId);
    List<ProductDto> searchByKeyword(Long storeId, String keyword);
}
