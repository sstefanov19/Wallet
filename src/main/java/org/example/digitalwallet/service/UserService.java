package org.example.digitalwallet.service;

import org.example.digitalwallet.dto.UserRequest;
import org.example.digitalwallet.model.User;
import org.example.digitalwallet.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public void register(UserRequest request) {

        User existingUser = userRepository.getUserByUsername(request.getUsername());

        if(existingUser != null) {
            // todo (change with custom exception later)
            throw new RuntimeException("User exists already!");
        }

        User user = User.builder().
                username(request.getUsername()).
                email(request.getEmail()).
                password(passwordEncoder.encode(request.getPassword())).
                membershipStatus(request.getStatus()).
                build();


        userRepository.saveUser(user);
    }
}
