package com.renko.service.impl;

import com.renko.configuration.JwtProvider;
import com.renko.domain.UserRole;
import com.renko.exceptions.UserException;
import com.renko.entities.UserEntity;
import com.renko.mapper.UserMapper;
import com.renko.payload.dto.UserDto;
import com.renko.repository.UserRepository;
import com.renko.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService
{
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @Override
    public UserDto getUserFromJwtToken(String token) throws UserException
    {
        String email = jwtProvider.getEmailFromToken(token);
        UserEntity userEntity = userRepository.findByEmail(email);

        if(userEntity == null)
        {
            throw new UserException("Invalid token!");
        }

        return UserMapper.toDto(userEntity);
    }

    @Override
    public UserDto getCurrentUser() throws UserException
    {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity currentUserEntity = userRepository.findByEmail(email);
        if(currentUserEntity == null)
        {
            throw new UserException("User not found!");
        }

        return  UserMapper.toDto(currentUserEntity);
    }

    @Override
    public UserDto getUserByEmail(String email) throws UserException
    {
        UserEntity userEntity = userRepository.findByEmail(email);
        if(userEntity == null)
        {
            throw new UserException("User not found!");
        }

        return  UserMapper.toDto(userEntity);
    }

    @Override
    public UserDto getUserById(Long id) throws Exception
    {
        return UserMapper.toDto(userRepository.findById(id).orElseThrow(() ->
        {
            return new Exception("User not found!");
        }));
    }

    @Override
    public List<UserDto> getAllUsers()
    {
        List<UserEntity> users = userRepository.findAll();
        return users.stream()
                    .map(UserMapper::toDto)
                    .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id)
    {
        userRepository.deleteById(id);
    }

    @Override
    public UserDto getAdminUser() throws UserException
    {
        return UserMapper.toDto(userRepository.findByRole(UserRole.ADMIN)
                .orElseThrow(() -> new UserException("Admin user not found!")));
    }
}
