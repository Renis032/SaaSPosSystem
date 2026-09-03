package com.renko.service;

import com.renko.entities.UserEntity;
import com.renko.payload.dto.ProductDto;
import com.renko.payload.dto.updates.ProductUpdateDto;

import java.util.List;

public interface ProductService
{
    ProductDto createProduct(ProductDto productDto, UserEntity userEntity) throws Exception;
    ProductDto updateProduct(Long id, ProductUpdateDto productDto) throws Exception;

    void deleteProduct(Long id, UserEntity userEntity) throws Exception;
    List<ProductDto> getProductsByStoreId(Long storeId);
    List<ProductDto> searchByKeyword(Long storeId, String keyword);
}
