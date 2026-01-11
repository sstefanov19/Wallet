package org.example.digitalwallet.service;

import org.example.digitalwallet.dto.LoginRequest;
import org.example.digitalwallet.dto.UserRequest;
import org.example.digitalwallet.exception.UserAlreadyExistsException;
import org.example.digitalwallet.model.MembershipStatus;
import org.example.digitalwallet.model.User;
import org.example.digitalwallet.repository.UserRepository;
import org.example.digitalwallet.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private UserService userService;

    // ========== Register Tests ==========

    @Test
    void testRegister_Success() {
        // Arrange
        UserRequest request = new UserRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        request.setStatus(MembershipStatus.FREE);

        when(userRepository.getUserByUsername("newuser")).thenReturn(null);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        // Act
        userService.register(request);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveUser(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals("newuser", capturedUser.getUsername());
        assertEquals("newuser@example.com", capturedUser.getEmail());
        assertEquals("encodedPassword", capturedUser.getPassword());
        assertEquals(MembershipStatus.FREE, capturedUser.getMembershipStatus());

        verify(passwordEncoder).encode("password123");
    }

    @Test
    void testRegister_WithPremiumStatus() {
        // Arrange
        UserRequest request = new UserRequest();
        request.setUsername("premiumuser");
        request.setEmail("premium@example.com");
        request.setPassword("securepass");
        request.setStatus(MembershipStatus.PREMIUM);

        when(userRepository.getUserByUsername("premiumuser")).thenReturn(null);
        when(passwordEncoder.encode("securepass")).thenReturn("encodedSecurePass");

        // Act
        userService.register(request);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveUser(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals(MembershipStatus.PREMIUM, capturedUser.getMembershipStatus());
    }

    @Test
    void testRegister_WithUltraStatus() {
        // Arrange
        UserRequest request = new UserRequest();
        request.setUsername("ultrauser");
        request.setEmail("ultra@example.com");
        request.setPassword("ultrapass");
        request.setStatus(MembershipStatus.ULTRA);

        when(userRepository.getUserByUsername("ultrauser")).thenReturn(null);
        when(passwordEncoder.encode("ultrapass")).thenReturn("encodedUltraPass");

        // Act
        userService.register(request);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveUser(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals(MembershipStatus.ULTRA, capturedUser.getMembershipStatus());
    }

    @Test
    void testRegister_UserAlreadyExists_ThrowsException() {
        // Arrange
        UserRequest request = new UserRequest();
        request.setUsername("existinguser");
        request.setEmail("existing@example.com");
        request.setPassword("password");

        User existingUser = User.builder()
                .id(1L)
                .username("existinguser")
                .email("existing@example.com")
                .build();

        when(userRepository.getUserByUsername("existinguser")).thenReturn(existingUser);

        // Act & Assert
        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.register(request)
        );

        assertEquals("User exists already!", exception.getMessage());
        verify(userRepository, never()).saveUser(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void testRegister_PasswordIsEncoded() {
        // Arrange
        String plainPassword = "myPlainPassword123";
        String encodedPassword = "encodedPasswordHash";

        UserRequest request = new UserRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword(plainPassword);
        request.setStatus(MembershipStatus.FREE);

        when(userRepository.getUserByUsername("testuser")).thenReturn(null);
        when(passwordEncoder.encode(plainPassword)).thenReturn(encodedPassword);

        // Act
        userService.register(request);

        // Assert
        verify(passwordEncoder).encode(plainPassword);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveUser(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals(encodedPassword, capturedUser.getPassword());
        assertNotEquals(plainPassword, capturedUser.getPassword());
    }

    @Test
    void testRegister_WithNullEmail() {
        // Arrange
        UserRequest request = new UserRequest();
        request.setUsername("userwithoutemail");
        request.setEmail(null);
        request.setPassword("password123");
        request.setStatus(MembershipStatus.FREE);

        when(userRepository.getUserByUsername("userwithoutemail")).thenReturn(null);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        // Act
        userService.register(request);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveUser(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertNull(capturedUser.getEmail());
        assertEquals("userwithoutemail", capturedUser.getUsername());
    }

    // ========== Login Tests ==========

    @Test
    void testLogin_Success() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        String expectedToken = "jwt.token.here";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateToken("testuser")).thenReturn(expectedToken);

        SecurityContextHolder.setContext(securityContext);

        // Act
        String token = userService.login(request);

        // Assert
        assertEquals(expectedToken, token);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken("testuser");
        verify(securityContext).setAuthentication(authentication);
    }

    @Test
    void testLogin_AuthenticationManagerCalledWithCorrectCredentials() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("user123");
        request.setPassword("pass456");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateToken("user123")).thenReturn("token");

        SecurityContextHolder.setContext(securityContext);

        // Act
        userService.login(request);

        // Assert
        ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(authCaptor.capture());

        UsernamePasswordAuthenticationToken capturedAuth = authCaptor.getValue();
        assertEquals("user123", capturedAuth.getPrincipal());
        assertEquals("pass456", capturedAuth.getCredentials());
    }

    @Test
    void testLogin_SetsAuthenticationInSecurityContext() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateToken("testuser")).thenReturn("token");

        SecurityContextHolder.setContext(securityContext);

        // Act
        userService.login(request);

        // Assert
        verify(securityContext).setAuthentication(authentication);
    }

    @Test
    void testLogin_ReturnsJwtToken() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("user");
        request.setPassword("pass");

        String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.example.token";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateToken("user")).thenReturn(expectedToken);

        SecurityContextHolder.setContext(securityContext);

        // Act
        String actualToken = userService.login(request);

        // Assert
        assertNotNull(actualToken);
        assertEquals(expectedToken, actualToken);
    }
}