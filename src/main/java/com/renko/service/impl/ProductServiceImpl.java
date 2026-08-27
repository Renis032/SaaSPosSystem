package com.renko.service.impl;

import com.renko.entities.ProductEntity;
import com.renko.entities.StoreEntity;
import com.renko.entities.UserEntity;
import com.renko.mapper.ProductMapper;
import com.renko.payload.dto.ProductDto;
import com.renko.repository.ProductRepository;
import com.renko.repository.StoreRepository;
import com.renko.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService
{
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;

    @Override
    public ProductDto createProduct(ProductDto productDto, UserEntity userEntity) throws Exception
    {
        StoreEntity storeEntity = storeRepository.findById(productDto.getStoreId())
                                                 .orElseThrow(() -> new Exception("Store not found."));

        ProductEntity productEntity = ProductMapper.toEntity(productDto, storeEntity);
        ProductEntity savedProduct = productRepository.save(productEntity);

        return ProductMapper.toDto(savedProduct);
    }

    @Override
    public ProductDto updateProduct(Long id, ProductDto productDto, UserEntity userEntity) throws Exception
    {

        ProductEntity productEntity = productRepository.findById(id)
                .orElseThrow(() -> new Exception("Product not found."));

        productEntity.setFromDto(productDto, productEntity.getStoreEntity());
        productEntity.setCreatedAt(productEntity.getCreatedAt()); // CHECK
        ProductEntity savedProduct = productRepository.save(productEntity);

        return ProductMapper.toDto(savedProduct);
    }

    @Override
    public void deleteProduct(Long id, UserEntity userEntity) throws Exception
    {
        ProductEntity productEntity = productRepository.findById(id)
                                                       .orElseThrow(() -> new Exception("Product not found."));

        productRepository.delete(productEntity);
    }

    @Override
    public List<ProductDto> getProductsByStoreId(Long storeId)
    {
        List<ProductEntity> productEntities = productRepository.findByStoreEntity_Id(storeId);

        return productEntities.stream()
                              .map(ProductMapper::toDto)
                              .collect(Collectors.toList());
    }

    @Override
    public List<ProductDto> searchByKeyword(Long storeId, String keyword)
    {
        List<ProductEntity> productEntities = productRepository.searchByKeyword(storeId, keyword);

        return productEntities.stream()
                .map(ProductMapper::toDto)
                .collect(Collectors.toList());
    }
}
