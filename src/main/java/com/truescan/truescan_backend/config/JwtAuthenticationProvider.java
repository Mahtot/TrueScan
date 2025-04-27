package com.truescan.truescan_backend.config;

import com.truescan.truescan_backend.security.CustomUserDetailsService;
import com.truescan.truescan_backend.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Autowired
    public JwtAuthenticationProvider(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String token = (String) authentication.getCredentials();

        if (token != null) {
            String username = jwtService.extractUsername(token);

            // Load the user details using the username from the token
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Now pass both token and userDetails to the isTokenValid method
            if (jwtService.isTokenValid(token, userDetails)) {
                return new UsernamePasswordAuthenticationToken(username, null, userDetails.getAuthorities());
            }
        }

        return null;  // Authentication failed
    }



    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);  // Support only username/password authentication type
    }
}
