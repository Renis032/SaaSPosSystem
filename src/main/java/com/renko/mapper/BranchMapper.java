package com.renko.mapper;

import com.renko.entities.BranchEntity;
import com.renko.entities.StoreEntity;
import com.renko.payload.dto.BranchDto;

public class BranchMapper
{
    public static BranchDto toDto(BranchEntity branchEntity)
    {
        return BranchDto.builder()
                        .id(branchEntity.getId())
                        .name(branchEntity.getName())
                        .address(branchEntity.getAddress())
                        .phone(branchEntity.getPhone())
                        .email(branchEntity.getEmail())
                        .workdays(branchEntity.getWorkdays())
                        .openTime(branchEntity.getOpenTime())
                        .closeTime(branchEntity.getCloseTime())
                        .createdAt(branchEntity.getCreatedAt())
                        .updatedAt(branchEntity.getUpdatedAt())
                        .storeId(branchEntity.getStoreEntity().getId() != null ? branchEntity.getStoreEntity().getId() : null)
                        .build();
    }

    public static BranchEntity toEntity(BranchDto branchDto, StoreEntity storeEntity)
    {
        return BranchEntity.builder()
                .id(branchDto.getId())
                .name(branchDto.getName())
                .address(branchDto.getAddress())
                .phone(branchDto.getPhone())
                .email(branchDto.getEmail())
                .storeEntity(storeEntity)
                .workdays(branchDto.getWorkdays())
                .openTime(branchDto.getOpenTime())
                .closeTime(branchDto.getCloseTime())
                .createdAt(branchDto.getCreatedAt())
                .updatedAt(branchDto.getUpdatedAt())
                .build();
    }
}
