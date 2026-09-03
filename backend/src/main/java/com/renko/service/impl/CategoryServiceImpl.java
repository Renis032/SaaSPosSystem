package com.renko.service.impl;

import com.renko.domain.UserRole;
import com.renko.entities.CategoryEntity;
import com.renko.entities.StoreEntity;
import com.renko.entities.UserEntity;
import com.renko.exceptions.UserException;
import com.renko.mapper.CategoryMapper;
import com.renko.mapper.UserMapper;
import com.renko.payload.dto.CategoryDto;
import com.renko.repository.CategoryRepository;
import com.renko.repository.StoreRepository;
import com.renko.service.CategoryService;
import com.renko.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService
{
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;
    private final UserService userService;


    @Override
    public CategoryDto createCategoryDto(CategoryDto categoryDto) throws Exception
    {
        UserEntity userEntity = UserMapper.toEntity(userService.getCurrentUser());
        StoreEntity storeEntity = storeRepository.findById(categoryDto.getStoreId())
                                                 .orElseThrow(() -> new Exception("Store not found"));

        if(false == isAuthenticated(userEntity, storeEntity))
        {
            throw new Exception("You dont have permission");
        }

        CategoryEntity categoryEntity = CategoryEntity.builder()
                                                      .name(categoryDto.getName())
                                                      .storeEntity(storeEntity)
                                                      .build();

        return CategoryMapper.toDto(categoryRepository.save(categoryEntity));
    }

    @Override
    public List<CategoryDto> getCategoriesByStore(Long storeId)
    {
        List<CategoryEntity> categories = categoryRepository.findByStoreEntity_Id(storeId);

        return categories.stream()
                         .map(CategoryMapper::toDto)
                         .collect(Collectors.toList());
    }

    @Override
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) throws Exception
    {
        CategoryEntity categoryEntity = categoryRepository.findById(id)
                                                          .orElseThrow(() -> new Exception("Category not found"));

        UserEntity userEntity = UserMapper.toEntity(userService.getCurrentUser());
        categoryEntity.setName(categoryDto.getName());

        if(false == isAuthenticated(userEntity, categoryEntity.getStoreEntity()))
        {
            throw new Exception("You dont have permission");
        }

        return CategoryMapper.toDto(categoryRepository.save(categoryEntity));
    }

    @Override
    public void deleteCategory(Long id) throws Exception
    {
        CategoryEntity categoryEntity = categoryRepository.findById(id)
                                                          .orElseThrow(() -> new Exception("Category not found"));

        UserEntity userEntity = UserMapper.toEntity(userService.getCurrentUser());
        if(false == isAuthenticated(userEntity, categoryEntity.getStoreEntity()))
        {
            throw new Exception("You dont have permission");
        }

        categoryRepository.delete(categoryEntity);
    }

    private boolean isAuthenticated(UserEntity userEntity, StoreEntity storeEntity) throws UserException
    {
        boolean isAdmin = userEntity.getRole() == UserRole.ADMIN;

        boolean isSameAdmin = storeEntity.getStoreAdmin() != null
                && userEntity.getId().equals(storeEntity.getStoreAdmin().getId());

        return isAdmin && isSameAdmin;
    }
}
