package com.renko.configuration;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.List;


public class JwtValidator extends OncePerRequestFilter
{
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException
    {
        // Extract the JWT token from the Authorization header of the incoming request
        String jwt = request.getHeader(JwtConstant.JWT_HEADER);

        // Remove the "Bearer " prefix because only the actual JWT token is needed for validation
        // Example: "Bearer eyJhbGciOi..." -> "eyJhbGciOi...
        if(jwt != null)
        {
            jwt = jwt.substring(BEARER_PREFIX.length());
            try
            {
                // Create the secret key used to verify that the JWT was signed by our backend
                SecretKey key = Keys.hmacShaKeyFor(JwtConstant.JWT_SECRET_KEY.getBytes());

                // Parse and validate the JWT
                // verifies the signature
                // checks that the token was not modified
                // extracts the data stored inside the token (claims)
                Claims claims = Jwts.parser()
                                .verifyWith(key)
                                .build()
                                .parseSignedClaims(jwt)
                                .getPayload();

                // Extract user information stored inside the JWT payload.
                String email = String.valueOf(claims.get("email"));
                // Extract user roles/permissions stored inside the JWT payload.
                String authorities = String.valueOf(claims.get("authorities"));

                // Convert JWT role strings into Spring Security authorities
                // Example: "ROLE_ADMIN,ROLE_USER" -> [ROLE_ADMIN, ROLE_USER]
                List<GrantedAuthority> auths = AuthorityUtils.commaSeparatedStringToAuthorityList(authorities);

                // Create an authenticated Spring Security object.
                // The password is null because authentication was already completed using JWT
                Authentication auth = new UsernamePasswordAuthenticationToken(email, null, auths);

                // Store the authenticated user in Spring's SecurityContext
                // Controllers and services can now access the logged-in user
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
            catch(Exception e)
            {
                throw new BadCredentialsException("Invalid JWT");
            }
            // Continue the request through the remaining security filters and eventually the controller
            filterChain.doFilter(request, response);
        }
    }
}
