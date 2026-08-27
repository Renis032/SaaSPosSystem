package com.renko.payload.dto;

import com.renko.domain.StoreStatus;
import com.renko.mapper.UserMapper;
import com.renko.entities.StoreEntity;
import com.renko.entities.StoreContactEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StoreDto
{
    private Long id;

    private String brandName;

    private UserDto storeAdminDto;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String description;

    private String storeType;

    private StoreStatus status;

    private StoreContactEntity contact;

    public void setFromEntity(StoreEntity storeEntity)
    {
        this.id = storeEntity.getId();
        this.brandName = storeEntity.getBrandName();
        this.storeAdminDto = UserMapper.toDto(storeEntity.getStoreAdmin());
        this.createdAt = storeEntity.getCreatedAt();
        this.updatedAt = storeEntity.getUpdatedAt();
        this.description = storeEntity.getDescription();
        this.storeType = storeEntity.getStoreType();
        this.status = storeEntity.getStatus();
        this.contact = storeEntity.getContact();
    }
}