package com.expenseapp.app.service;

import com.expenseapp.app.entities.User;
import com.expenseapp.app.repositories.UserRepository;
import com.expenseapp.app.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImp implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Username = email from JwT

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        if (!user.isEnabled()) {
            throw new UsernameNotFoundException("User account is disabled: " + username);
        }

        return AuthenticatedUser.builder()
                .user(user)  // pass the full User entity
                .build();

    }
}
