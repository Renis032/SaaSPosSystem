package com.renko.service;

import com.renko.exceptions.UserException;
import com.renko.model.StoreEntity;
import com.renko.model.UserEntity;
import com.renko.payload.dto.StoreDto;

import java.util.List;

public interface StoreService
{
    StoreDto createStore(StoreDto storeDto, UserEntity userEntity);
    StoreDto getStoreById(Long id) throws Exception;
    List<StoreDto> getAllStores();
    StoreEntity getStoreByAdmin() throws UserException;
    StoreDto updateStore(Long id, StoreDto storeDto) throws UserException;
    void deleteStore(Long id) throws UserException;
    StoreDto getStoreByEmployee() throws UserException;
}
