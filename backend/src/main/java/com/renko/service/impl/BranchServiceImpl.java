package com.renko.service.impl;

import com.renko.entities.BranchEntity;
import com.renko.entities.StoreEntity;
import com.renko.entities.UserEntity;
import com.renko.exceptions.ExceptionMessages;
import com.renko.exceptions.UserException;
import com.renko.mapper.BranchMapper;
import com.renko.payload.dto.BranchDto;
import com.renko.payload.dto.UserDto;
import com.renko.payload.dto.updates.BranchUpdateDto;
import com.renko.repository.BranchRepository;
import com.renko.repository.StoreRepository;
import com.renko.repository.UserRepository;
import com.renko.service.BranchService;
import com.renko.service.StoreAccessService;
import com.renko.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BranchServiceImpl implements BranchService
{
    private final BranchRepository branchRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final StoreAccessService storeAccessService;

    @Override
    public BranchDto createBranch(BranchDto branchDto) throws UserException
    {
        UserDto currentUser = userService.getCurrentUser();
        StoreEntity storeEntity = resolveStoreForCreate(branchDto, currentUser);

        BranchEntity branch = BranchMapper.toEntity(branchDto, storeEntity);

        if(branchDto.getManagerId() != null)
        {
            UserEntity manager = userRepository.findById(branchDto.getManagerId())
                    .orElseThrow(() -> ExceptionMessages.notFound("Manager", branchDto.getManagerId(), "create branch"));
            branch.setUserEntity(manager);
        }

        BranchEntity savedBranch = branchRepository.save(branch);

        return BranchMapper.toDto(savedBranch);
    }

    @Override
    public BranchDto updateBranch(Long id, BranchUpdateDto branchDto) throws Exception
    {
        BranchEntity existingBranch = branchRepository.findById(id)
                .orElseThrow(() -> ExceptionMessages.notFound("Branch", id, "update"));

        if(branchDto.getStoreId() != null)
        {
            StoreEntity storeEntity = storeRepository.findById(branchDto.getStoreId())
                    .orElseThrow(() -> ExceptionMessages.notFound("Store", branchDto.getStoreId(),
                            "update branchId=" + id));
            existingBranch.setStoreEntity(storeEntity);
        }

        existingBranch.updateFrom(branchDto);
        BranchEntity savedBranch = branchRepository.save(existingBranch);

        return BranchMapper.toDto(savedBranch);
    }

    @Override
    public BranchDto getBranchById(Long id) throws Exception
    {
        BranchEntity branchEntity = branchRepository.findById(id)
                .orElseThrow(() -> ExceptionMessages.notFound("Branch", id));

        return BranchMapper.toDto(branchEntity);
    }

    @Override
    public List<BranchDto> getAllBranches()
    {
        return branchRepository.findAll().stream()
                .map(BranchMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteBranch(Long id) throws Exception
    {
        BranchEntity branchEntity = branchRepository.findById(id)
                .orElseThrow(() -> ExceptionMessages.notFound("Branch", id, "delete"));

        branchRepository.delete(branchEntity);
    }

    @Override
    public void deleteAllBranches()
    {
        branchRepository.deleteAll();
    }

    @Override
    public List<BranchDto> getAllBranchesByStoreId(Long id) throws Exception
    {
        storeAccessService.requireStoreAccess(id);
        return branchRepository.findByStoreEntity_Id(id).stream()
                       .map(BranchMapper::toDto)
                       .collect(Collectors.toList());
    }

    private StoreEntity resolveStoreForCreate(BranchDto branchDto, UserDto currentUser) throws UserException
    {
        if(branchDto.getStoreId() != null)
        {
            storeAccessService.requireStoreAccess(branchDto.getStoreId());
            return storeRepository.findById(branchDto.getStoreId())
                    .orElseThrow(() -> ExceptionMessages.notFound("Store", branchDto.getStoreId(), "create branch"));
        }

        StoreEntity storeEntity = storeRepository.findByStoreAdmin_Id(currentUser.getId());
        if(storeEntity == null)
        {
            throw ExceptionMessages.required(
                    "storeId",
                    "A store is required before creating a branch. Create a store first or provide a valid storeId."
            );
        }
        storeAccessService.requireStoreAccess(storeEntity.getId());
        return storeEntity;
    }
}
