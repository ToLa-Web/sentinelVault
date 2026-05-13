package com.tola.sentinelvault.identity.infrastructure.security;

import com.tola.sentinelvault.identity.domain.model.Email;
import com.tola.sentinelvault.identity.domain.model.User;
import com.tola.sentinelvault.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Bridges Spring Security's UserDetailsService with the domain UserRepository.
 * Used by Spring Security's auth manager during form/basic auth if needed.
 * JWT-based flows use JwtFilter directly and bypass this.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(Email.of(email))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return new CustomUserPrincipal(
                user.getId(),
                user.getEmail().value(),
                user.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}