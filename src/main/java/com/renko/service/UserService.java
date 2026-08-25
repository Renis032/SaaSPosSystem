package com.renko.service;

import com.renko.exceptions.UserException;
import com.renko.model.UserEntity;

import java.util.List;

public interface UserService
{
    UserEntity getUserFromJwtToken(String token) throws UserException;
    UserEntity getCurrentUser() throws UserException;
    UserEntity getUserByEmail(String email) throws UserException;
    UserEntity getUserById(Long id) throws UserException, Exception;
    List<UserEntity> getAllUsers();
}
