package com.renko.service.impl;

import com.renko.configuration.JwtProvider;
import com.renko.domain.UserRole;
import com.renko.exceptions.UserException;
import com.renko.mapper.UserMapper;
import com.renko.entities.UserEntity;
import com.renko.payload.dto.AuthRequestDto;
import com.renko.payload.dto.UserDto;
import com.renko.payload.response.AuthResponse;
import com.renko.repository.UserRepository;
import com.renko.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService
{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    public AuthResponse signUp(AuthRequestDto authRequestDto) throws UserException
    {
        UserEntity userEntity = userRepository.findByEmail(authRequestDto.getEmail());
        if(userEntity != null)
        {
            throw UserException.withDetail(
                    "Email is already registered",
                    "email",
                    authRequestDto.getEmail()
            );
        }

        if(authRequestDto.getRole() == UserRole.ADMIN &&
           userRepository.existsByRole(UserRole.ADMIN))
        {
            throw UserException.withDetail(
                    "Only one ADMIN user is allowed in the system",
                    "role",
                    UserRole.ADMIN
            );
        }

        UserEntity newUserEntity = new UserEntity();
        newUserEntity.setEmail(authRequestDto.getEmail());
        newUserEntity.setPassword(passwordEncoder.encode(authRequestDto.getPassword()));
        newUserEntity.setRole(authRequestDto.getRole());
        newUserEntity.setPhoneNumber(authRequestDto.getPhoneNumber());
        newUserEntity.setFullName(authRequestDto.getFullName());

        newUserEntity.setCreatedAt(LocalDateTime.now());
        newUserEntity.setUpdatedAt(LocalDateTime.now());
        newUserEntity.setLastLoginAt(LocalDateTime.now());

        // Save the User entity to the database
        // Spring Data JPA generates the SQL required to insert the user
        UserEntity savedUserEntity = userRepository.save(newUserEntity);

        // Create an Authentication object representing the newly registered user
        // Load UserDetails so the JWT includes the user's authorities/role
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(savedUserEntity.getEmail());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        // Store the Authentication object in Spring Security's SecurityContext
        // The SecurityContext represents the currently authenticated user for this request.
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate a JWT containing information about the authenticated user
        // The frontend can use this token in future requests
        String jwt = jwtProvider.generateToken(authentication);

        // Create the response that will be returned to the frontend
        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(jwt);
        authResponse.setMessage("Registered successfully!");


        // Convert the database entity into a DTO before sending it to the client
        // DTOs prevent us from exposing the database entity directly through the API
        authResponse.setUser(UserMapper.toDto(savedUserEntity));

        return authResponse;
    }

    @Override
    public AuthResponse login(AuthRequestDto authRequestDto) throws UserException
    {
        String email = authRequestDto.getEmail();
        String password = authRequestDto.getPassword();

        Authentication authentication = authenticate(email, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        String role = authorities.iterator().next().getAuthority();
        String jwt = jwtProvider.generateToken(authentication);

        UserEntity userEntity = userRepository.findByEmail(email);
        userEntity.setLastLoginAt(LocalDateTime.now());
        userRepository.save(userEntity);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(jwt);
        authResponse.setMessage("Login successfully!");
        authResponse.setUser(UserMapper.toDto(userEntity));

        return authResponse;
    }

    private Authentication authenticate(String email, String password) throws UserException
    {
        try
        {
            // Ask our UserDetailsService to find the user in the database
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
            if(!passwordEncoder.matches(password, userDetails.getPassword()))
            {
                throw UserException.withDetail(
                        "Wrong password for the given email",
                        "email",
                        email
                );
            }

            // Create an authenticated Spring Security Authentication object
            // This Authentication object can later be placed into
            // Spring Security's SecurityContext and used to generate a JWT
            return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        }
        catch(UsernameNotFoundException e)
        {
            throw UserException.withDetail(
                    "No user exists with the given email",
                    "email",
                    email
            );
        }
    }
}
