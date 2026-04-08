package org.example.digitalwallet.service;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.AllArgsConstructor;
import org.example.digitalwallet.dto.LoginRequest;
import org.example.digitalwallet.dto.UserRequest;
import org.example.digitalwallet.exception.RateLimitExceededException;
import org.example.digitalwallet.exception.UserAlreadyExistsException;
import org.example.digitalwallet.model.User;
import org.example.digitalwallet.repository.UserRepository;
import org.example.digitalwallet.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@AllArgsConstructor
@Service
public class UserService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil  jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public void register(UserRequest request) {
        checkForExistingUser(request.username());

        User user = User.builder().
                username(request.username()).
                email(request.email()).
                password(passwordEncoder.encode(request.password())).
                membershipStatus(request.status()).
                build();

        userRepository.saveUser(user);
    }

//    @RateLimiter(name = "loginRateLimiter" , fallbackMethod = "loggingFallBack")
    public String login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return jwtUtil.generateToken(request.username());

    }

    public User getUserByUsername(String username) {
        return userRepository.getUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with such name isnt found"));
    }

    private String loggingFallBack(LoginRequest request, RequestNotPermitted t) {
        throw new RateLimitExceededException("Rate limit for logging in exceeded try again later");
    }

    private void checkForExistingUser(String username) {
        Optional<User> existingUser = userRepository.getUserByUsername(username);

        if(existingUser.isPresent()) {
            throw new UserAlreadyExistsException("User exists already!");
        }
    }
}
