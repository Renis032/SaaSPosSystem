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
import com.renko.service.StoreService;
import com.renko.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService
{
    private final StoreRepository storeRepository;
    private final UserService userService;

    @Override
    public StoreDto createStore(StoreDto storeDto, UserEntity userEntity)
    {
        StoreEntity storeEntity = StoreMapper.toEntity(storeDto, userEntity);

        StoreEntity savedStoreEntity = storeRepository.save(storeEntity);
        return StoreMapper.toDto(savedStoreEntity);
    }

    @Override
    public StoreDto getStoreById(Long id) throws Exception
    {
        StoreEntity storeEntity = storeRepository.findById(id)
                                                 .orElseThrow(
                                                 () -> new Exception("Store not found!"));

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
            throw new UserException("You dont have permission");
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
                                                 .orElseThrow(() -> new UserException("Store not found"));

        storeRepository.delete(storeEntity);
    }

    @Override
    public StoreDto getStoreByEmployee() throws Exception
    {
        UserDto currentUser = userService.getCurrentUser();
        if(currentUser == null)
        {
            throw  new UserException("No permission.");
        }

        StoreEntity storeEntity = storeRepository.findById(currentUser.getStoreId())
                                                 .orElseThrow(() -> new Exception("No store found"));

        return StoreMapper.toDto(storeEntity);
    }

    @Override
    public StoreDto moderateStore(Long id, StoreStatus storeStatus) throws Exception
    {
        StoreEntity storeEntity = storeRepository.findById(id)
                                                 .orElseThrow(() -> new Exception("Store not found"));

        storeEntity.setStatus(storeStatus);
        StoreEntity savedStore = storeRepository.save(storeEntity);

        return StoreMapper.toDto(savedStore);
    }
}
