package com.renko.service.impl;

import com.renko.entities.CategoryEntity;
import com.renko.entities.ProductEntity;
import com.renko.entities.StoreEntity;
import com.renko.entities.UserEntity;
import com.renko.mapper.ProductMapper;
import com.renko.payload.dto.ProductDto;
import com.renko.payload.dto.updates.ProductUpdateDto;
import com.renko.repository.CategoryRepository;
import com.renko.repository.ProductRepository;
import com.renko.repository.StoreRepository;
import com.renko.repository.UserRepository;
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
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    public ProductDto createProduct(ProductDto productDto, UserEntity userEntity) throws Exception
    {
        // Ensure caller exists in DB (do not trust a detached mapper entity)
        if(userEntity != null && userEntity.getId() != null)
        {
            userRepository.findById(userEntity.getId())
                    .orElseThrow(() -> new Exception("User not found with id: " + userEntity.getId()
                            + "; cannot create product '" + productDto.getName() + "'"));
        }

        StoreEntity storeEntity = storeRepository.findById(productDto.getStoreId())
                                                 .orElseThrow(() -> new Exception("Store not found with id: " + productDto.getStoreId()
                                                         + "; cannot create product '" + productDto.getName() + "'"));

        CategoryEntity categoryEntity = null;
        if(productDto.getCategoryId() != null)
        {
            categoryEntity = categoryRepository.findById(productDto.getCategoryId())
                    .orElseThrow(() -> new Exception("Category not found with id: " + productDto.getCategoryId()
                            + "; cannot create product '" + productDto.getName() + "'"));
        }

        ProductEntity productEntity = ProductMapper.toEntity(productDto, storeEntity, categoryEntity);
        ProductEntity savedProduct = productRepository.save(productEntity);

        return ProductMapper.toDto(savedProduct);
    }

    @Override
    public ProductDto getProductById(Long id) throws Exception
    {
        ProductEntity productEntity = productRepository.findById(id)
                .orElseThrow(() -> new Exception("Product not found with id: " + id));
        return ProductMapper.toDto(productEntity);
    }

    @Override
    public List<ProductDto> getAllProducts()
    {
        return productRepository.findAll().stream()
                .map(ProductMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProductDto updateProduct(Long id, ProductUpdateDto productDto) throws Exception
    {
        ProductEntity productEntity = productRepository.findById(id)
                .orElseThrow(() -> new Exception("Product not found with id: " + id + "; cannot update product"));

        productEntity.updateFrom(productDto);

        if(productDto.getCategoryId() != null)
        {
            CategoryEntity categoryEntity = categoryRepository.findById(productDto.getCategoryId())
                    .orElseThrow(() -> new Exception("Category not found with id: " + productDto.getCategoryId()
                            + "; cannot update productId=" + id));
            productEntity.setCategoryEntity(categoryEntity);
        }

        productEntity.setCreatedAt(productEntity.getCreatedAt()); // CHECK
        ProductEntity savedProduct = productRepository.save(productEntity);

        return ProductMapper.toDto(savedProduct);
    }

    @Override
    public void deleteProduct(Long id, UserEntity userEntity) throws Exception
    {
        ProductEntity productEntity = productRepository.findById(id)
                                                       .orElseThrow(() -> new Exception("Product not found with id: " + id
                                                               + "; cannot delete (requested by userId=" + (userEntity != null ? userEntity.getId() : null) + ")"));

        productRepository.delete(productEntity);
    }

    @Override
    public void deleteAllProducts()
    {
        productRepository.deleteAll();
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
