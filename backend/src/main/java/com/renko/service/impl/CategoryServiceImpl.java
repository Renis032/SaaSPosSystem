package com.renko.service.impl;

import com.renko.domain.UserRole;
import com.renko.entities.CategoryEntity;
import com.renko.entities.StoreEntity;
import com.renko.entities.UserEntity;
import com.renko.exceptions.UserException;
import com.renko.mapper.CategoryMapper;
import com.renko.payload.dto.CategoryDto;
import com.renko.payload.dto.UserDto;
import com.renko.repository.CategoryRepository;
import com.renko.repository.StoreRepository;
import com.renko.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final UserService userService;


    @Override
    public CategoryDto createCategoryDto(CategoryDto categoryDto) throws Exception
    {
        UserEntity userEntity = loadCurrentUser();
        StoreEntity storeEntity = storeRepository.findById(categoryDto.getStoreId())
                                                 .orElseThrow(() -> new Exception("Store not found with id: " + categoryDto.getStoreId()
                                                         + "; cannot create category '" + categoryDto.getName() + "'"));

        if(false == isAuthenticated(userEntity, storeEntity))
        {
            throw new Exception("You do not have permission to create a category for storeId=" + categoryDto.getStoreId()
                    + " as userId=" + userEntity.getId() + " with role=" + userEntity.getRole());
        }

        CategoryEntity categoryEntity = CategoryEntity.builder()
                                                      .name(categoryDto.getName())
                                                      .storeEntity(storeEntity)
                                                      .build();

        return CategoryMapper.toDto(categoryRepository.save(categoryEntity));
    }

    @Override
    public CategoryDto getCategoryById(Long id) throws Exception
    {
        CategoryEntity categoryEntity = categoryRepository.findById(id)
                .orElseThrow(() -> new Exception("Category not found with id: " + id));
        return CategoryMapper.toDto(categoryEntity);
    }

    @Override
    public List<CategoryDto> getAllCategories()
    {
        return categoryRepository.findAll().stream()
                .map(CategoryMapper::toDto)
                .collect(Collectors.toList());
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
                                                          .orElseThrow(() -> new Exception("Category not found with id: " + id + "; cannot update"));

        UserEntity userEntity = loadCurrentUser();
        categoryEntity.setName(categoryDto.getName());

        if(false == isAuthenticated(userEntity, categoryEntity.getStoreEntity()))
        {
            Long storeId = categoryEntity.getStoreEntity() != null ? categoryEntity.getStoreEntity().getId() : null;
            throw new Exception("You do not have permission to update categoryId=" + id
                    + " on storeId=" + storeId + " as userId=" + userEntity.getId()
                    + " with role=" + userEntity.getRole());
        }

        return CategoryMapper.toDto(categoryRepository.save(categoryEntity));
    }

    @Override
    public void deleteCategory(Long id) throws Exception
    {
        CategoryEntity categoryEntity = categoryRepository.findById(id)
                                                          .orElseThrow(() -> new Exception("Category not found with id: " + id + "; cannot delete"));

        UserEntity userEntity = loadCurrentUser();
        if(false == isAuthenticated(userEntity, categoryEntity.getStoreEntity()))
        {
            Long storeId = categoryEntity.getStoreEntity() != null ? categoryEntity.getStoreEntity().getId() : null;
            throw new Exception("You do not have permission to delete categoryId=" + id
                    + " on storeId=" + storeId + " as userId=" + userEntity.getId()
                    + " with role=" + userEntity.getRole());
        }

        categoryRepository.delete(categoryEntity);
    }

    @Override
    public void deleteAllCategories()
    {
        categoryRepository.deleteAll();
    }

    private UserEntity loadCurrentUser() throws Exception
    {
        UserDto currentUser = userService.getCurrentUser();
        return userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new Exception("User not found with id: " + currentUser.getId()));
    }

    private boolean isAuthenticated(UserEntity userEntity, StoreEntity storeEntity) throws UserException
    {
        boolean isAdmin = userEntity.getRole() == UserRole.ADMIN;

        boolean isSameAdmin = storeEntity.getStoreAdmin() != null
                && userEntity.getId().equals(storeEntity.getStoreAdmin().getId());

        // Platform ADMIN or the store's own admin (OWNER creating their store) may manage categories
        return isAdmin || isSameAdmin;
    }
}
