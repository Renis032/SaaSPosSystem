package com.renko.service;

import com.renko.exceptions.UserException;
import com.renko.model.User;

import java.util.List;

public interface UserService
{
    User getUserFromJwtToken(String token) throws UserException;
    User getCurrentUser() throws UserException;
    User getUserByEmail(String email) throws UserException;
    User getUserById(Long id) throws UserException, Exception;
    List<User> getAllUsers();
}
