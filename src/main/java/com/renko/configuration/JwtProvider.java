package com.renko.configuration;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Service
public class JwtProvider
{
    private static final long JWT_EXPIRATION_TIME = 86400000; // 24 hours
    private final SecretKey key = Keys.hmacShaKeyFor(JwtConstant.JWT_SECRET_KEY.getBytes());

    // Creates a signed JWT containing user identity and permissions
    public String generateToken(Authentication authentication)
    {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        String roles = populateAuthorities(authorities);

        return Jwts
                .builder()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION_TIME))
                .claim("email", authentication.getName())
                .claim("authorities", roles)
                .signWith(key)
                .compact();
    }

    public String getEmailFromToken(String jwt)
    {
        jwt = removeBearerPrefix(jwt);
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();

        // Extract user information stored inside the JWT payload.
        return String.valueOf(claims.get("email"));
    }

    // Converts Spring Security authorities into a format stored inside the JWT
    private String populateAuthorities(Collection<? extends GrantedAuthority> authorities)
    {
        Set<String> auths = new HashSet<>();
        for(GrantedAuthority authority : authorities)
        {
            auths.add(authority.getAuthority());
        }

        return String.join(",", auths);
    }

    private String removeBearerPrefix(String jwt)
    {
        if(jwt.startsWith("Bearer "))
        {
            return jwt.substring(7);
        }

        return jwt;
    }
}
