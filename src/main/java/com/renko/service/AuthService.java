package com.renko.service;

import com.renko.exceptions.UserException;
import com.renko.payload.dto.UserDto;
import com.renko.payload.response.AuthResponse;

public interface AuthService
{
    AuthResponse signUp(UserDto userDto) throws UserException;
    AuthResponse login(UserDto userDto);
}
