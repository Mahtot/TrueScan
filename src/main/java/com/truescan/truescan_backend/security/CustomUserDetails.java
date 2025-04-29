package com.truescan.truescan_backend.security;

import com.truescan.truescan_backend.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collections;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private String email;
    private String password;
    private List<GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.email = user.getEmail();
        this.password = user.getPassword();
        // Map the role to a GrantedAuthority
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole()));
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public List<GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // Assuming account is never expired
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // Assuming account is never locked
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // Assuming credentials are never expired
    }

    @Override
    public boolean isEnabled() {
        return true;  // Assuming user is always enabled
    }
}
