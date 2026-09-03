package com.renko.mapper;

import com.renko.entities.StoreEntity;
import com.renko.entities.UserEntity;
import com.renko.payload.dto.StoreDto;

public class StoreMapper
{
    public static StoreDto toDto(StoreEntity storeEntity)
    {
        StoreDto storeDto = new StoreDto();
        storeDto.setFromEntity(storeEntity);

        return storeDto;
    }

    public static StoreEntity toEntity(StoreDto storeDto, UserEntity storeAdmin)
    {
        StoreEntity storeEntity = new StoreEntity();
        storeEntity.setFromDto(storeDto);
        storeEntity.setStoreAdmin(storeAdmin);

        return storeEntity;
    }
}
