package service;

import entity.Freelancer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomFreelancerDetails implements UserDetails {

    private final Freelancer freelancer;

    public CustomFreelancerDetails(Freelancer freelancer) {
        this.freelancer = freelancer;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_FREELANCER")
        );
    }

    @Override
    public String getPassword() {
        return freelancer.getPassword();
    }

    @Override
    public String getUsername() {
        return freelancer.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    public Freelancer getUser() {
        return freelancer;
    }
}