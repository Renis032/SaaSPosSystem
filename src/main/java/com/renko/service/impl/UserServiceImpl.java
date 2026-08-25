package com.renko.service.impl;

import com.renko.configuration.JwtProvider;
import com.renko.exceptions.UserException;
import com.renko.model.UserEntity;
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
    public UserEntity getUserFromJwtToken(String token) throws UserException
    {
        String email = jwtProvider.getEmailFromToken(token);
        UserEntity userEntity = userRepository.findByEmail(email);

        if(userEntity == null)
        {
            throw new UserException("Invalid token!");
        }

        return userEntity;
    }

    @Override
    public UserEntity getCurrentUser() throws UserException
    {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity currentUserEntity = userRepository.findByEmail(email);
        if(currentUserEntity == null)
        {
            throw new UserException("User not found!");
        }

        return currentUserEntity;
    }

    @Override
    public UserEntity getUserByEmail(String email) throws UserException
    {
        UserEntity userEntity = userRepository.findByEmail(email);
        if(userEntity == null)
        {
            throw new UserException("User not found!");
        }

        return userEntity;
    }

    @Override
    public UserEntity getUserById(Long id) throws UserException, Exception
    {
        return userRepository.findById(id).orElseThrow(() ->
        {
            return new Exception("User not found!");
        });
    }

    @Override
    public List<UserEntity> getAllUsers()
    {
        return userRepository.findAll();
    }
}
