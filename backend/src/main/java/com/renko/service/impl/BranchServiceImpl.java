package com.renko.service.impl;

import com.renko.entities.BranchEntity;
import com.renko.entities.StoreEntity;
import com.renko.entities.UserEntity;
import com.renko.exceptions.UserException;
import com.renko.mapper.BranchMapper;
import com.renko.payload.dto.BranchDto;
import com.renko.payload.dto.UserDto;
import com.renko.payload.dto.updates.BranchUpdateDto;
import com.renko.repository.BranchRepository;
import com.renko.repository.StoreRepository;
import com.renko.repository.UserRepository;
import com.renko.service.BranchService;
import com.renko.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService
{
    private final BranchRepository branchRepository;
    private final StoreRepository storeRepository;
    private final UserService userService;

    @Override
    public BranchDto createBranch(BranchDto branchDto) throws UserException
    {
        UserDto currentUser = userService.getCurrentUser();
        StoreEntity storeEntity = storeRepository.findByStoreAdmin_Id(currentUser.getId());

        BranchEntity branch = BranchMapper.toEntity(branchDto, storeEntity);
        BranchEntity savedBranch = branchRepository.save(branch);

        return BranchMapper.toDto(savedBranch);
    }

    @Override
    public BranchDto updateBranch(Long id, BranchUpdateDto branchDto) throws Exception
    {
        BranchEntity existingBranch = branchRepository.findById(id)
                .orElseThrow(() -> new Exception("Branch not found with id: " + id + "; cannot update"));

        existingBranch.updateFrom(branchDto);
        BranchEntity savedBranch = branchRepository.save(existingBranch);

        return BranchMapper.toDto(savedBranch);
    }

    @Override
    public BranchDto getBranchById(Long id) throws Exception
    {
        BranchEntity branchEntity = branchRepository.findById(id)
                .orElseThrow(() -> new Exception("Branch not found with id: " + id));

        return BranchMapper.toDto(branchEntity);
    }

    @Override
    public void deleteBranch(Long id) throws Exception
    {
        BranchEntity branchEntity = branchRepository.findById(id)
                .orElseThrow(() -> new Exception("Branch not found with id: " + id + "; cannot delete"));

        branchRepository.delete(branchEntity);
    }

    @Override
    public List<BranchDto> getAllBranchesByStoreId(Long id)
    {
        List<BranchEntity> branches = branchRepository.findAll();
        return branches.stream()
                       .map(BranchMapper::toDto)
                       .collect(Collectors.toList());
    }
}
