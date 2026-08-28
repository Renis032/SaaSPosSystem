package com.renko.entities;

import com.renko.payload.dto.ProductDto;
import com.renko.payload.dto.updates.ProductUpdateDto;
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

    public void updateFrom(ProductUpdateDto dto)
    {
        if(dto.getName() != null)
        {
            this.name = dto.getName();
        }

        if(dto.getSku() != null)
        {
            this.sku = dto.getSku();
        }

        if(dto.getDescription() != null)
        {
            this.description = dto.getDescription();
        }

        if(dto.getMaxRetailPrice() != null)
        {
            this.maxRetailPrice = dto.getMaxRetailPrice();
        }

        if(dto.getSellingPrice() != null)
        {
            this.sellingPrice = dto.getSellingPrice();
        }

        if(dto.getBrand() != null)
        {
            this.brand = dto.getBrand();
        }

        if(dto.getImageUrl() != null)
        {
            this.imageUrl = dto.getImageUrl();
        }

        if(dto.getCategoryEntity() != null)
        {
            this.categoryEntity = dto.getCategoryEntity();
        }

        this.updatedAt = LocalDateTime.now();
    }
}
