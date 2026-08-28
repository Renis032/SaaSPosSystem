package com.renko.controller;

import com.renko.exceptions.UserException;
import com.renko.mapper.UserMapper;
import com.renko.entities.UserEntity;
import com.renko.payload.dto.UserDto;
import com.renko.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController
{
    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getUserProfile(@RequestHeader("Authorization") String jwt) throws UserException
    {
        return ResponseEntity.ok(userService.getUserFromJwtToken(jwt));
    }

    @GetMapping("/admin")
    public ResponseEntity<UserDto> getAdminUser() throws UserException {
        return ResponseEntity.ok(userService.getAdminUser());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@RequestHeader("Authorization") String jwt,
                                               @PathVariable Long id) throws UserException, Exception
    {
        return ResponseEntity.ok( userService.getUserById(id));
    }

    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable Long id) throws UserException, Exception
    {
        userService.deleteById(id);
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() throws UserException
    {
        List<UserDto> usersDto = userService.getAllUsers();
        return ResponseEntity.ok(usersDto);
    }
}
