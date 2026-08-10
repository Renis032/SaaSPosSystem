package com.renko.service.impl;

import com.renko.configuration.JwtProvider;
import com.renko.exceptions.UserException;
import com.renko.model.User;
import com.renko.repository.UserRepository;
import com.renko.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService
{
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @Override
    public User getUserFromJwtToken(String token) throws UserException
    {
        String email = jwtProvider.getEmailFromToken(token);
        User user = userRepository.findByEmail(email);

        if(user == null)
        {
            throw new UserException("Invalid token!");
        }

        return user;
    }

    @Override
    public User getCurrentUser() throws UserException
    {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email);
        if(currentUser == null)
        {
            throw new UserException("User not found!");
        }

        return currentUser;
    }

    @Override
    public User getUserByEmail(String email) throws UserException
    {
        User user = userRepository.findByEmail(email);
        if(user == null)
        {
            throw new UserException("User not found!");
        }

        return user;
    }

    @Override
    public User getUserById(Long id) throws UserException, Exception
    {
        return userRepository.findById(id).orElseThrow(() ->
        {
            return new Exception("User not found!");
        });
    }

    @Override
    public List<User> getAllUsers()
    {
        return userRepository.findAll();
    }
}
