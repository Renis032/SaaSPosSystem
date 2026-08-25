package com.renko.controller;

import com.renko.exceptions.UserException;
import com.renko.mapper.UserMapper;
import com.renko.model.UserEntity;
import com.renko.payload.dto.UserDto;
import com.renko.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController
{
    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getUserProfile(@RequestHeader("Authorization") String jwt) throws UserException
    {
        UserEntity userEntity = userService.getUserFromJwtToken(jwt);
        return ResponseEntity.ok(UserMapper.toDto(userEntity));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@RequestHeader("Authorization") String jwt,
                                               @PathVariable Long id) throws UserException, Exception
    {
        UserEntity userEntity = userService.getUserById(id);
        return ResponseEntity.ok(UserMapper.toDto(userEntity));
    }

}
