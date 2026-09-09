package com.renko.service.impl;

import com.renko.entities.CategoryEntity;
import com.renko.entities.ProductEntity;
import com.renko.entities.StoreEntity;
import com.renko.entities.UserEntity;
import com.renko.exceptions.ExceptionMessages;
import com.renko.mapper.ProductMapper;
import com.renko.payload.dto.PageResponse;
import com.renko.payload.dto.ProductDto;
import com.renko.payload.dto.updates.ProductUpdateDto;
import com.renko.repository.CategoryRepository;
import com.renko.repository.ProductRepository;
import com.renko.repository.StoreRepository;
import com.renko.repository.UserRepository;
import com.renko.service.AuditLogService;
import com.renko.service.ProductService;
import com.renko.service.StoreAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private final StoreAccessService storeAccessService;
    private final AuditLogService auditLogService;

    @Override
    public ProductDto createProduct(ProductDto productDto, UserEntity userEntity) throws Exception
    {
        if(userEntity != null && userEntity.getId() != null)
        {
            userRepository.findById(userEntity.getId())
                    .orElseThrow(() -> ExceptionMessages.notFound(
                            "User",
                            userEntity.getId(),
                            "create product '" + productDto.getName() + "'"
                    ));
        }

        if(productDto.getStoreId() == null)
        {
            throw ExceptionMessages.required(
                    "storeId",
                    "storeId is required to create a product. Create a store first."
            );
        }

        storeAccessService.requireStoreAccess(productDto.getStoreId());

        StoreEntity storeEntity = storeRepository.findById(productDto.getStoreId())
                .orElseThrow(() -> ExceptionMessages.notFound(
                        "Store",
                        productDto.getStoreId(),
                        "create product '" + productDto.getName() + "'"
                ));

        CategoryEntity categoryEntity = resolveCategory(
                productDto.getCategoryId(),
                storeEntity.getId(),
                "create product '" + productDto.getName() + "'"
        );

        ProductEntity productEntity = ProductMapper.toEntity(productDto, storeEntity, categoryEntity);
        ProductEntity savedProduct = productRepository.save(productEntity);

        return ProductMapper.toDto(savedProduct);
    }

    @Override
    public ProductDto getProductById(Long id) throws Exception
    {
        ProductEntity productEntity = productRepository.findById(id)
                .orElseThrow(() -> ExceptionMessages.notFound("Product", id));
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
                .orElseThrow(() -> ExceptionMessages.notFound("Product", id, "update"));

        String before = "name=" + productEntity.getName()
                + "; sellingPrice=" + productEntity.getSellingPrice()
                + "; discountPercentage=" + productEntity.getDiscountPercentage();

        if(productEntity.getStoreEntity() != null)
        {
            storeAccessService.requireStoreAccess(productEntity.getStoreEntity().getId());
        }

        productEntity.updateFrom(productDto);

        if(productDto.getCategoryId() != null)
        {
            Long storeId = productEntity.getStoreEntity() != null ? productEntity.getStoreEntity().getId() : null;
            CategoryEntity categoryEntity = resolveCategory(
                    productDto.getCategoryId(),
                    storeId,
                    "update productId=" + id
            );
            productEntity.setCategoryEntity(categoryEntity);
        }

        productEntity.setCreatedAt(productEntity.getCreatedAt());
        ProductEntity savedProduct = productRepository.save(productEntity);

        String after = "name=" + savedProduct.getName()
                + "; sellingPrice=" + savedProduct.getSellingPrice()
                + "; discountPercentage=" + savedProduct.getDiscountPercentage();
        auditLogService.recordChange(
                savedProduct.getStoreEntity() != null ? savedProduct.getStoreEntity().getId() : null,
                "PRODUCT_UPDATE",
                "Product",
                String.valueOf(id),
                before,
                after,
                null
        );

        return ProductMapper.toDto(savedProduct);
    }

    @Override
    public void deleteProduct(Long id, UserEntity userEntity) throws Exception
    {
        ProductEntity productEntity = productRepository.findById(id)
                .orElseThrow(() -> ExceptionMessages.notFound(
                        "Product",
                        id,
                        "delete (requested by userId=" + (userEntity != null ? userEntity.getId() : null) + ")"
                ));

        productRepository.delete(productEntity);
    }

    @Override
    public void deleteAllProducts()
    {
        productRepository.deleteAll();
    }

    @Override
    public List<ProductDto> getProductsByStoreId(Long storeId) throws Exception
    {
        storeAccessService.requireStoreAccess(storeId);
        List<ProductEntity> productEntities = productRepository.findByStoreEntity_Id(storeId);

        return productEntities.stream()
                              .map(ProductMapper::toDto)
                              .collect(Collectors.toList());
    }

    @Override
    public List<ProductDto> searchByKeyword(Long storeId, String keyword) throws Exception
    {
        storeAccessService.requireStoreAccess(storeId);
        List<ProductEntity> productEntities = productRepository.searchByKeyword(storeId, keyword);

        return productEntities.stream()
                .map(ProductMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<ProductDto> getProductsByStoreIdPaged(Long storeId, int page, int size, String q) throws Exception
    {
        storeAccessService.requireStoreAccess(storeId);
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 20 : Math.min(size, 100);
        PageRequest pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<ProductEntity> result = productRepository.searchByStore(storeId, blankToNull(q), pageable);
        return PageResponse.of(
                result.getContent().stream().map(ProductMapper::toDto).collect(Collectors.toList()),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements()
        );
    }

    private static String blankToNull(String value)
    {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private CategoryEntity resolveCategory(Long categoryId, Long storeId, String action) throws Exception
    {
        if(categoryId == null)
        {
            return null;
        }

        CategoryEntity categoryEntity = categoryRepository.findById(categoryId)
                .orElseThrow(() -> ExceptionMessages.notFound("Category", categoryId, action));

        if(storeId != null
           && (categoryEntity.getStoreEntity() == null
               || false == storeId.equals(categoryEntity.getStoreEntity().getId())))
        {
            throw ExceptionMessages.mismatch(
                    "Category does not belong to the product's store",
                    "categoryId", categoryId,
                    "categoryStoreId", categoryEntity.getStoreEntity() != null
                            ? categoryEntity.getStoreEntity().getId()
                            : null,
                    "storeId", storeId
            );
        }

        return categoryEntity;
    }
}
