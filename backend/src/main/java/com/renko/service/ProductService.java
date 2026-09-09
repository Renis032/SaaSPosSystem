package com.renko.service;

import com.renko.entities.UserEntity;
import com.renko.payload.dto.PageResponse;
import com.renko.payload.dto.ProductDto;
import com.renko.payload.dto.updates.ProductUpdateDto;

import java.util.List;

public interface ProductService
{
    ProductDto createProduct(ProductDto productDto, UserEntity userEntity) throws Exception;
    ProductDto getProductById(Long id) throws Exception;
    List<ProductDto> getAllProducts();
    ProductDto updateProduct(Long id, ProductUpdateDto productDto) throws Exception;

    void deleteProduct(Long id, UserEntity userEntity) throws Exception;
    void deleteAllProducts();
    List<ProductDto> getProductsByStoreId(Long storeId) throws Exception;
    List<ProductDto> searchByKeyword(Long storeId, String keyword) throws Exception;
    PageResponse<ProductDto> getProductsByStoreIdPaged(Long storeId, int page, int size, String q) throws Exception;
}
