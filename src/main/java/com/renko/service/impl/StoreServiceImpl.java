package com.renko.service.impl;

import com.renko.exceptions.UserException;
import com.renko.mapper.StoreMapper;
import com.renko.model.StoreContactEntity;
import com.renko.model.StoreEntity;
import com.renko.model.UserEntity;
import com.renko.payload.dto.StoreDto;
import com.renko.repository.StoreRepository;
import com.renko.service.StoreService;
import com.renko.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
        StoreDto savedStoreDto = StoreMapper.toDto(savedStoreEntity);
        return savedStoreDto;
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
    public List<StoreDto> getAllStores()
    {
        List<StoreEntity> stores = storeRepository.findAll();
        List<StoreDto> storeDtos = stores.stream().map(StoreMapper::toDto).collect(Collectors.toList());
        return storeDtos;
    }

    @Override
    public StoreEntity getStoreByAdmin() throws UserException
    {
        UserEntity admin = userService.getCurrentUser();
        return storeRepository.findByStoreAdmin(admin.getId());
    }

    @Override
    public StoreDto updateStore(Long id, StoreDto storeDto) throws UserException
    {
        UserEntity currentUser = userService.getCurrentUser();
        StoreEntity store = storeRepository.findByStoreAdmin(currentUser.getId());
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
        StoreEntity storeEntity = getStoreByAdmin();
        storeRepository.delete(storeEntity);
    }

    @Override
    public StoreDto getStoreByEmployee() throws UserException
    {
        UserEntity currentUser = userService.getCurrentUser();
        if(currentUser == null)
        {
            throw  new UserException("No permission.");
        }
        
        return StoreMapper.toDto(currentUser.getStoreEntity());
    }
}
