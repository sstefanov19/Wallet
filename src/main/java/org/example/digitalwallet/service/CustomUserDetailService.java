package org.example.digitalwallet.service;
import lombok.RequiredArgsConstructor;
import org.example.digitalwallet.repository.UserRepository;
import org.example.digitalwallet.model.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @NotNull
    @Override
    public UserDetails loadUserByUsername(@NotNull String username) throws UsernameNotFoundException {

        User user = userRepository.getUserByUsername(username);

        if(user == null) {
            throw new UsernameNotFoundException("User isn't found in the database");
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();

    }
}