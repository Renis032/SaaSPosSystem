package com.renko.service.impl;

import com.renko.entities.UserEntity;
import com.renko.payload.dto.ProductDto;
import com.renko.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService
{
    @Override
    public ProductDto createProduct(ProductDto productDto, UserEntity userEntity)
    {
        return null;
    }

    @Override
    public ProductDto updateProduct(Long id, ProductDto productDto, UserEntity userEntity)
    {
        return null;
    }

    @Override
    public void deleteProduct(Long id, UserEntity userEntity)
    {

    }

    @Override
    public List<ProductDto> getProductsByStoreId(Long storeId)
    {
        return List.of();
    }

    @Override
    public List<ProductDto> searchByKeyword(Long storeId, String keyword)
    {
        return List.of();
    }
}
