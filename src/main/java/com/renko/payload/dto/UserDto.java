package com.renko.payload.dto;

import com.renko.domain.UserRole;
import com.renko.model.User;
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

    public void setFromUser(User user)
    {
        id = user.getId();
        fullName = user.getFullName();
        email = user.getEmail();
        phoneNumber = user.getPhoneNumber();
        role = user.getRole();
        createdAt = user.getCreatedAt();
        updatedAt = user.getUpdatedAt();
        lastLoginAt = user.getLastLoginAt();
    }
}
