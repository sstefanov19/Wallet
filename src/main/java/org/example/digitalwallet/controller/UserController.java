package org.example.digitalwallet.controller;

import org.example.digitalwallet.dto.UserRequest;
import org.example.digitalwallet.service.UserService;
import org.example.digitalwallet.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public ResponseEntity<Map<String , String>> register(@RequestBody  UserRequest request) {
        userService.register(request);

        String token = jwtUtil.generateToken(request.getEmail());

        Map<String , String> response = new HashMap<>();

        response.put("message", "User registered succesffully");
        response.put("token" , token);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
