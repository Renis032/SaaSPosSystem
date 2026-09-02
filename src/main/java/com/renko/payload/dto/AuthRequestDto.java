package com.renko.payload.dto;

import com.renko.domain.UserRole;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequestDto
{
    private String email;
    private String password;
    private String fullName;
    private String phoneNumber;
    private UserRole role;
}