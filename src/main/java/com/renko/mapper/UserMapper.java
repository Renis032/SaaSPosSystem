package com.renko.mapper;

import com.renko.model.UserEntity;
import com.renko.payload.dto.UserDto;

public class UserMapper
{
    public static UserDto toDto(UserEntity savedUserEntity)
    {
        UserDto userDto = new UserDto();
        userDto.setFromUser(savedUserEntity);
        return userDto;
    }
}
