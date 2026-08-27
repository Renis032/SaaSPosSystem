package com.renko.payload.dto;

import com.renko.domain.UserRole;
import com.renko.entities.UserEntity;
import lombok.Data;

import java.time.LocalDateTime;

//DTO = Data Transfer Object
// Only what the front end needs
@Data
public class UserDto
{
    private Long id;
    private String fullName;
    private String email;
    private String password;

    private String phoneNumber;

    private UserRole role;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;

    public void setFromEntity(UserEntity userEntity)
    {
        id = userEntity.getId();
        fullName = userEntity.getFullName();
        email = userEntity.getEmail();
        phoneNumber = userEntity.getPhoneNumber();
        role = userEntity.getRole();
        createdAt = userEntity.getCreatedAt();
        updatedAt = userEntity.getUpdatedAt();
        lastLoginAt = userEntity.getLastLoginAt();
    }
}
