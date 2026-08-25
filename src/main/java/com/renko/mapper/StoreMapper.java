package com.renko.mapper;

import com.renko.model.StoreEntity;
import com.renko.model.UserEntity;
import com.renko.payload.dto.StoreDto;

public class StoreMapper
{
    public static StoreDto toDto(StoreEntity storeEntity)
    {
        StoreDto storeDto = new StoreDto();
        storeDto.setFromStore(storeEntity);

        return storeDto;
    }

    public static StoreEntity toEntity(StoreDto storeDto, UserEntity storeAdmin)
    {
        StoreEntity storeEntity = new StoreEntity();
        storeEntity.setId(storeDto.getId());
        storeEntity.setBrandName(storeDto.getBrandName());
        storeEntity.setDescription(storeDto.getDescription());
        storeEntity.setStoreAdmin(storeAdmin);
        storeEntity.setStoreType(storeDto.getStoreType());
        storeEntity.setCreatedAt(storeDto.getCreatedAt());
        storeEntity.setUpdatedAt(storeDto.getUpdatedAt());

        return storeEntity;
    }
}
