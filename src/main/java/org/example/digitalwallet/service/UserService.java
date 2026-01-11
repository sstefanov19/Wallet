package org.example.digitalwallet.service;

import lombok.AllArgsConstructor;
import org.example.digitalwallet.dto.LoginRequest;
import org.example.digitalwallet.dto.UserRequest;
import org.example.digitalwallet.exception.UserAlreadyExistsException;
import org.example.digitalwallet.model.User;
import org.example.digitalwallet.repository.UserRepository;
import org.example.digitalwallet.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class UserService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil  jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public void register(UserRequest request) {

        User existingUser = userRepository.getUserByUsername(request.getUsername());

        if(existingUser != null) {
            throw new UserAlreadyExistsException("User exists already!");
        }

        User user = User.builder().
                username(request.getUsername()).
                email(request.getEmail()).
                password(passwordEncoder.encode(request.getPassword())).
                membershipStatus(request.getStatus()).
                build();


        userRepository.saveUser(user);
    }

    public String login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return jwtUtil.generateToken(request.getUsername());

    }
}
