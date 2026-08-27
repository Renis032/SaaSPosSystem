package com.renko.entities;

import com.renko.payload.dto.ProductDto;
import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String sku;

    private String description;

    private Double maxRetailPrice;

    private Double sellingPrice;

    private String brand;

    private String imageUrl;

    @ManyToOne
    private CategoryEntity categoryEntity;

    @ManyToOne
    private StoreEntity storeEntity;
    private Long storeId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate()
    {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate()
    {
        updatedAt = LocalDateTime.now();
    }

    public void setFromDto(ProductDto productDto, StoreEntity storeEntity)
    {
        this.id = productDto.getId();
        this.name = productDto.getName();
        this.sku = productDto.getSku();
        this.description = productDto.getDescription();
        this.maxRetailPrice = productDto.getMaxRetailPrice();
        this.sellingPrice = productDto.getSellingPrice();
        this.brand = productDto.getBrand();
        this.imageUrl = productDto.getImageUrl();
        this.categoryEntity = productDto.getCategoryEntity();
        this.storeEntity = storeEntity;
        this.storeId = productDto.getStoreId();
        this.createdAt = productDto.getCreatedAt();
        this.updatedAt = productDto.getUpdatedAt();
    }
}
