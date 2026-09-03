package com.renko.service.impl;

import com.renko.domain.StoreStatus;
import com.renko.exceptions.UserException;
import com.renko.mapper.StoreMapper;
import com.renko.entities.StoreContactEntity;
import com.renko.entities.StoreEntity;
import com.renko.entities.UserEntity;
import com.renko.payload.dto.StoreDto;
import com.renko.payload.dto.UserDto;
import com.renko.repository.StoreRepository;
import com.renko.repository.UserRepository;
import com.renko.service.StoreService;
import com.renko.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService
{
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    public StoreDto createStore(StoreDto storeDto, UserEntity userEntity)
    {
        UserEntity storeAdmin = userRepository.findById(userEntity.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cannot create store: user not found with id: " + userEntity.getId()
                ));

        StoreEntity storeEntity = StoreMapper.toEntity(storeDto, storeAdmin);
        StoreEntity savedStoreEntity = storeRepository.save(storeEntity);

        // Link owner/admin to store so order/shift flows can resolve storeId from JWT user
        storeAdmin.setStoreEntity(savedStoreEntity);
        userRepository.save(storeAdmin);

        return StoreMapper.toDto(savedStoreEntity);
    }

    @Override
    public StoreDto getStoreById(Long id) throws Exception
    {
        StoreEntity storeEntity = storeRepository.findById(id)
                                                 .orElseThrow(
                                                 () -> new Exception("Store not found with id: " + id));

        return StoreMapper.toDto(storeEntity);
    }

    @Override
    public List<StoreEntity> getAllStores()
    {
        return storeRepository.findAll();
    }

    @Override
    public StoreEntity getStoreByAdmin() throws UserException
    {
        UserDto admin = userService.getCurrentUser();
        return storeRepository.findByStoreAdmin_Id(admin.getId());
    }

    @Override
    public StoreDto updateStore(Long id, StoreDto storeDto) throws UserException
    {
        UserDto currentUser = userService.getCurrentUser();
        StoreEntity store = storeRepository.findByStoreAdmin_Id(currentUser.getId());
        if(store == null)
        {
            throw UserException.withDetails(
                    "You do not have permission to update a store. No store is linked to the current admin user.",
                    Map.of(
                            "userId", currentUser.getId(),
                            "requestedStoreId", id
                    )
            );
        }

        store.setBrandName(storeDto.getBrandName());
        store.setDescription(storeDto.getDescription());

        if(storeDto.getStoreType() != null)
        {
            store.setStoreType(storeDto.getStoreType());
        }

        if(storeDto.getContact() != null)
        {
            StoreContactEntity storeContact = StoreContactEntity.builder()
                                                                .address(storeDto.getContact().getAddress())
                                                                .phone(storeDto.getContact().getPhone())
                                                                .email(storeDto.getContact().getEmail())
                                                                .build();
            store.setContact(storeContact);
        }

        StoreEntity updatedStoreEntity = storeRepository.save(store);

        return StoreMapper.toDto(updatedStoreEntity);
    }

    @Override
    public void deleteStore(Long id) throws UserException
    {
        StoreEntity storeEntity = storeRepository.findById(id)
                                                 .orElseThrow(() -> new UserException(
                                                         "Store not found with id: " + id,
                                                         Map.of("storeId", id)
                                                 ));

        storeRepository.delete(storeEntity);
    }

    @Override
    public StoreDto getStoreByEmployee() throws Exception
    {
        UserDto currentUser = userService.getCurrentUser();
        if(currentUser == null)
        {
            throw new UserException("No authenticated user found; cannot resolve employee store.");
        }

        if(currentUser.getStoreId() == null)
        {
            throw UserException.withDetails(
                    "Current user is not linked to any store",
                    Map.of(
                            "userId", currentUser.getId(),
                            "email", currentUser.getEmail()
                    )
            );
        }

        StoreEntity storeEntity = storeRepository.findById(currentUser.getStoreId())
                                                 .orElseThrow(() -> new Exception(
                                                         "Store not found with id: " + currentUser.getStoreId()
                                                         + " for employee userId=" + currentUser.getId()
                                                 ));

        return StoreMapper.toDto(storeEntity);
    }

    @Override
    public StoreDto moderateStore(Long id, StoreStatus storeStatus) throws Exception
    {
        StoreEntity storeEntity = storeRepository.findById(id)
                                                 .orElseThrow(() -> new Exception(
                                                         "Store not found with id: " + id
                                                         + "; cannot set status to " + storeStatus
                                                 ));

        storeEntity.setStatus(storeStatus);
        StoreEntity savedStore = storeRepository.save(storeEntity);

        return StoreMapper.toDto(savedStore);
    }

    @Override
    public void deleteAllStores() throws UserException
    {
        storeRepository.deleteAll();
    }
}
