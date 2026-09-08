package com.renko.service.impl;

import com.renko.domain.UserRole;
import com.renko.entities.CategoryEntity;
import com.renko.entities.StoreEntity;
import com.renko.entities.UserEntity;
import com.renko.exceptions.ExceptionMessages;
import com.renko.exceptions.UserException;
import com.renko.mapper.CategoryMapper;
import com.renko.payload.dto.CategoryDto;
import com.renko.payload.dto.UserDto;
import com.renko.repository.CategoryRepository;
import com.renko.repository.StoreRepository;
import com.renko.repository.UserRepository;
import com.renko.service.CategoryService;
import com.renko.service.StoreAccessService;
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
    private final StoreAccessService storeAccessService;


    @Override
    public CategoryDto createCategoryDto(CategoryDto categoryDto) throws Exception
    {
        if(categoryDto.getStoreId() == null)
        {
            throw ExceptionMessages.required(
                    "storeId",
                    "storeId is required to create a category. Create a store first."
            );
        }

        storeAccessService.requireStoreAccess(categoryDto.getStoreId());

        UserEntity userEntity = loadCurrentUser();
        StoreEntity storeEntity = storeRepository.findById(categoryDto.getStoreId())
                .orElseThrow(() -> ExceptionMessages.notFound(
                        "Store",
                        categoryDto.getStoreId(),
                        "create category '" + categoryDto.getName() + "'"
                ));

        if(false == isAuthenticated(userEntity, storeEntity))
        {
            throw UserException.withDetails(
                    "You do not have permission to create a category for this store",
                    ExceptionMessages.ctx(
                            "storeId", categoryDto.getStoreId(),
                            "userId", userEntity.getId(),
                            "role", userEntity.getRole()
                    )
            );
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
                .orElseThrow(() -> ExceptionMessages.notFound("Category", id));
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
    public List<CategoryDto> getCategoriesByStore(Long storeId) throws Exception
    {
        storeAccessService.requireStoreAccess(storeId);
        List<CategoryEntity> categories = categoryRepository.findByStoreEntity_Id(storeId);

        return categories.stream()
                         .map(CategoryMapper::toDto)
                         .collect(Collectors.toList());
    }

    @Override
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) throws Exception
    {
        CategoryEntity categoryEntity = categoryRepository.findById(id)
                .orElseThrow(() -> ExceptionMessages.notFound("Category", id, "update"));

        UserEntity userEntity = loadCurrentUser();
        categoryEntity.setName(categoryDto.getName());

        if(false == isAuthenticated(userEntity, categoryEntity.getStoreEntity()))
        {
            Long storeId = categoryEntity.getStoreEntity() != null ? categoryEntity.getStoreEntity().getId() : null;
            throw UserException.withDetails(
                    "You do not have permission to update this category",
                    ExceptionMessages.ctx(
                            "categoryId", id,
                            "storeId", storeId,
                            "userId", userEntity.getId(),
                            "role", userEntity.getRole()
                    )
            );
        }

        return CategoryMapper.toDto(categoryRepository.save(categoryEntity));
    }

    @Override
    public void deleteCategory(Long id) throws Exception
    {
        CategoryEntity categoryEntity = categoryRepository.findById(id)
                .orElseThrow(() -> ExceptionMessages.notFound("Category", id, "delete"));

        UserEntity userEntity = loadCurrentUser();
        if(false == isAuthenticated(userEntity, categoryEntity.getStoreEntity()))
        {
            Long storeId = categoryEntity.getStoreEntity() != null ? categoryEntity.getStoreEntity().getId() : null;
            throw UserException.withDetails(
                    "You do not have permission to delete this category",
                    ExceptionMessages.ctx(
                            "categoryId", id,
                            "storeId", storeId,
                            "userId", userEntity.getId(),
                            "role", userEntity.getRole()
                    )
            );
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
                .orElseThrow(() -> ExceptionMessages.notFound("User", currentUser.getId()));
    }

    private boolean isAuthenticated(UserEntity userEntity, StoreEntity storeEntity) throws UserException
    {
        if(storeEntity == null)
        {
            throw ExceptionMessages.required(
                    "storeId",
                    "Category has no linked store; cannot verify permissions"
            );
        }

        boolean isAdmin = userEntity.getRole() == UserRole.ADMIN;

        boolean isSameAdmin = storeEntity.getStoreAdmin() != null
                && userEntity.getId().equals(storeEntity.getStoreAdmin().getId());

        return isAdmin || isSameAdmin;
    }
}
