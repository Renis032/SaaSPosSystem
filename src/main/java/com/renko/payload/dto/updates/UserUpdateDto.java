package com.renko.payload.dto.updates;

import com.renko.domain.UserRole;
import com.renko.entities.StoreEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserUpdateDto
{
    private String fullName;
    private String email;
    private String password;

    private String phoneNumber;

    private UserRole role;

    private StoreEntity storeEntity;
}
