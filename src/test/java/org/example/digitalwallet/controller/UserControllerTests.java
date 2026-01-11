package org.example.digitalwallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.digitalwallet.dto.LoginRequest;
import org.example.digitalwallet.dto.UserRequest;
import org.example.digitalwallet.exception.UserAlreadyExistsException;
import org.example.digitalwallet.model.MembershipStatus;
import org.example.digitalwallet.service.CustomUserDetailService;
import org.example.digitalwallet.service.UserService;
import org.example.digitalwallet.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailService customUserDetailService;

    // ========== Register Tests ==========

    @Test
    void testRegister_Success() throws Exception {
        // Arrange
        UserRequest request = new UserRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setStatus(MembershipStatus.FREE);

        doNothing().when(userService).register(any(UserRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("User registered successfully"));

        verify(userService, times(1)).register(any(UserRequest.class));
    }

    @Test
    void testRegister_WithPremiumStatus() throws Exception {
        // Arrange
        UserRequest request = new UserRequest();
        request.setUsername("premiumuser");
        request.setEmail("premium@example.com");
        request.setPassword("securepass");
        request.setStatus(MembershipStatus.PREMIUM);

        doNothing().when(userService).register(any(UserRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("User registered successfully"));

        verify(userService, times(1)).register(any(UserRequest.class));
    }

    @Test
    void testRegister_WithUltraStatus() throws Exception {
        // Arrange
        UserRequest request = new UserRequest();
        request.setUsername("ultrauser");
        request.setEmail("ultra@example.com");
        request.setPassword("password");
        request.setStatus(MembershipStatus.ULTRA);

        doNothing().when(userService).register(any(UserRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("User registered successfully"));

        verify(userService, times(1)).register(any(UserRequest.class));
    }

    @Test
    void testRegister_UserAlreadyExists() throws Exception {
        // Arrange
        UserRequest request = new UserRequest();
        request.setUsername("existinguser");
        request.setEmail("existing@example.com");
        request.setPassword("password");
        request.setStatus(MembershipStatus.FREE);

        doThrow(new UserAlreadyExistsException("User exists already!"))
                .when(userService).register(any(UserRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        verify(userService, times(1)).register(any(UserRequest.class));
    }

    @Test
    void testRegister_WithNullEmail() throws Exception {
        // Arrange
        UserRequest request = new UserRequest();
        request.setUsername("noemailuser");
        request.setEmail(null);
        request.setPassword("password");
        request.setStatus(MembershipStatus.FREE);

        doNothing().when(userService).register(any(UserRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("User registered successfully"));

        verify(userService, times(1)).register(any(UserRequest.class));
    }

    @Test
    void testRegister_InvalidJson() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).register(any(UserRequest.class));
    }

    // ========== Login Tests ==========

    @Test
    void testLogin_Success() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        String expectedToken = "jwt.token.here";

        when(userService.login(any(LoginRequest.class))).thenReturn(expectedToken);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedToken));

        verify(userService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void testLogin_ReturnsJwtToken() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("user123");
        request.setPassword("securepass");

        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.example.token";

        when(userService.login(any(LoginRequest.class))).thenReturn(jwtToken);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(jwtToken));

        verify(userService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void testLogin_BadCredentials() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(userService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void testLogin_InvalidJson() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).login(any(LoginRequest.class));
    }

    @Test
    void testLogin_EmptyCredentials() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("");
        request.setPassword("");

        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(userService, times(1)).login(any(LoginRequest.class));
    }
}