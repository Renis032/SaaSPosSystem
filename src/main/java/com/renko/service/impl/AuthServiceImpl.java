//package com.renko.service.impl;
//
//import com.renko.configuration.JwtProvider;
//import com.renko.domain.UserRole;
//import com.renko.exceptions.UserException;
//import com.renko.mapper.UserMapper;
//import com.renko.model.User;
//import com.renko.payload.dto.UserDto;
//import com.renko.payload.response.AuthResponse;
//import com.renko.repository.UserRepository;
//import com.renko.service.AuthService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//
//@Service
//@RequiredArgsConstructor
//public class AuthServiceImpl implements AuthService
//{
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final JwtProvider jwtProvider;
//    private final CustomUserDetailsService customUserDetailsService;
//
//    @Override
//    public AuthResponse signUp(UserDto userDto) throws UserException
//    {
//        User user = userRepository.findByEmail(userDto.getEmail());
//        if(user != null)
//        {
//            throw new UserException("Email is already registered!");
//        }
//
//        if(userDto.getRole().equals(UserRole.ADMIN))
//        {
//            throw new UserException("Only one ADMIN allowed!");
//        }
//
//        User newUser = new User();
//        newUser.setEmail(userDto.getEmail());
//        newUser.setPassword(userDto.getPassword());
//        newUser.setRole(userDto.getRole());
//        newUser.setPhoneNumber(userDto.getPhoneNumber());
//        newUser.setFullName(userDto.getFullName());
//
//        newUser.setUpdatedAt(LocalDateTime.now());
//
//        User savedUser = userRepository.save(newUser);
//
//        Authentication authentication = new UsernamePasswordAuthenticationToken(userDto.getEmail(), userDto.getPassword());
//
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        String jwt = jwtProvider.generateToken(authentication);
//
//        AuthResponse authResponse = new AuthResponse();
//        authResponse.setJwt(jwt);
//        authResponse.setMessage("Registered successfully!");
//        authResponse.setUser(UserMapper.toDto(savedUser));
//
//        return null;
//    }
//
//    @Override
//    public AuthResponse login(UserDto userDto)
//    {
//        return null;
//    }
//}
