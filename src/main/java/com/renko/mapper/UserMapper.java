package com.renko.mapper;

import com.renko.model.User;
import com.renko.payload.dto.UserDto;

public class UserMapper
{
    public static UserDto toDto(User savedUser)
    {
        UserDto userDto = new UserDto();
        userDto.setFromUser(savedUser);
        return userDto;
    }
}
