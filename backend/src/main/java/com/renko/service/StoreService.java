package com.renko.service;

import com.renko.domain.StoreStatus;
import com.renko.exceptions.UserException;
import com.renko.entities.StoreEntity;
import com.renko.entities.UserEntity;
import com.renko.payload.dto.StoreDto;

import java.util.List;

public interface StoreService
{
    StoreDto createStore(StoreDto storeDto, UserEntity userEntity);
    StoreDto getStoreById(Long id) throws Exception;
    List<StoreEntity> getAllStores();
    StoreEntity getStoreByAdmin() throws UserException;
    StoreDto updateStore(Long id, StoreDto storeDto) throws UserException;
    void deleteStore(Long id) throws UserException;
    StoreDto getStoreByEmployee() throws Exception;
    StoreDto moderateStore(Long id, StoreStatus storeStatus) throws Exception;
    void deleteAllStores() throws UserException;
}
