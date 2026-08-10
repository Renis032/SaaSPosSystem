package com.renko.service.impl;

import com.renko.model.User;
import com.renko.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService
{
    @Autowired // Injects the UserRepository bean managed by Spring
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException
    {
        User user = userRepository.findByEmail(email);
        if(user == null)
        {
            throw new UsernameNotFoundException("User not found!");
        }

        // Convert the application's role into a Spring Security authority
        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().toString());

        Collection<GrantedAuthority> authorities = Collections.singletonList(authority); // User's granted authorities

        // Return Spring Security's built-in UserDetails implementation
        // Spring uses this object to authenticate the user and check permissions
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities);
    }
}
