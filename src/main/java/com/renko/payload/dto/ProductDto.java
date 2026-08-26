package com.renko.payload.dto;

import com.renko.entities.CategoryEntity;
import com.renko.entities.StoreEntity;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

public class ProductDto
{
    private Long id;

    private String name;

    private String sku;

    private String description;

    private Double maxRetailPrice;

    private Double sellingPrice;

    private String brand;

    private String imageUrl;

    private CategoryEntity categoryEntity;

    private StoreEntity storeEntity;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
