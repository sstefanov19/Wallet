package org.example.digitalwallet.service;

import org.example.digitalwallet.dto.DepositRequest;
import org.example.digitalwallet.dto.WalletRequest;
import org.example.digitalwallet.exception.UserNotAuthenticatedException;
import org.example.digitalwallet.model.User;
import org.example.digitalwallet.model.Wallet;
import org.example.digitalwallet.model.WalletCurrency;
import org.example.digitalwallet.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletServiceTests {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private WalletService walletService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    // ========== Create Wallet Tests ==========

    @Test
    void testCreateWallet_Success() {
        String username = "testuser";
        Long userId = 1L;
        BigDecimal initialBalance = BigDecimal.valueOf(100.00);

        WalletRequest request = new WalletRequest(WalletCurrency.EUR, initialBalance);

        User mockUser = User.builder()
                .id(userId)
                .username(username)
                .email("test@example.com")
                .build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
        when(userService.getUserByUsername(username)).thenReturn(mockUser);

        walletService.createWallet(request);

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository, times(1)).createWallet(walletCaptor.capture());

        Wallet capturedWallet = walletCaptor.getValue();
        assertEquals(userId, capturedWallet.getUserId());
        assertEquals(WalletCurrency.EUR, capturedWallet.getCurrency());
        assertEquals(initialBalance, capturedWallet.getBalance());
        assertNotNull(capturedWallet.getCreatedAt());

        verify(emailService).sendWalletCreationEmail(
                "test@example.com",
                username,
                "EUR",
                "100.0");
    }

    @Test
    void testCreateWallet_WithNullCurrency_DefaultsToEUR() {
        String username = "testuser";
        Long userId = 1L;
        BigDecimal initialBalance = BigDecimal.valueOf(50.00);

        WalletRequest request = new WalletRequest(null, initialBalance);

        User mockUser = User.builder()
                .id(userId)
                .username(username)
                .email("test@example.com")
                .build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
        when(userService.getUserByUsername(username)).thenReturn(mockUser);

        walletService.createWallet(request);

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).createWallet(walletCaptor.capture());

        assertEquals(WalletCurrency.EUR, walletCaptor.getValue().getCurrency());
    }

    @Test
    void testCreateWallet_UserNotAuthenticated_ThrowsException() {
        WalletRequest request = new WalletRequest(WalletCurrency.EUR, BigDecimal.valueOf(100.00));

        when(securityContext.getAuthentication()).thenReturn(null);

        UserNotAuthenticatedException exception = assertThrows(
                UserNotAuthenticatedException.class,
                () -> walletService.createWallet(request));

        assertEquals("User was not authenticated! Try logging in", exception.getMessage());
        verify(walletRepository, never()).createWallet(any());
        verify(emailService, never()).sendWalletCreationEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testCreateWallet_UserWithNullEmail_DoesNotSendEmail() {
        String username = "testuser";
        Long userId = 1L;

        WalletRequest request = new WalletRequest(WalletCurrency.EUR, BigDecimal.valueOf(200.00));

        User mockUser = User.builder()
                .id(userId)
                .username(username)
                .email(null)
                .build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
        when(userService.getUserByUsername(username)).thenReturn(mockUser);

        walletService.createWallet(request);

        verify(walletRepository, times(1)).createWallet(any(Wallet.class));
        verify(emailService, never()).sendWalletCreationEmail(anyString(), anyString(), anyString(), anyString());
    }

    // ========== Deposit to Wallet Tests ==========

    @Test
    void testDepositToWallet_Success() {
        String username = "testuser";
        Long userId = 1L;
        Long walletId = 10L;
        BigDecimal initialBalance = BigDecimal.valueOf(100.00);
        BigDecimal depositAmount = BigDecimal.valueOf(50.00);
        BigDecimal expectedNewBalance = BigDecimal.valueOf(150.00);

        User mockUser = User.builder()
                .id(userId)
                .username(username)
                .email("test@example.com")
                .build();

        Wallet mockWallet = Wallet.builder()
                .id(walletId)
                .userId(userId)
                .currency(WalletCurrency.EUR)
                .balance(initialBalance)
                .build();

        DepositRequest request = new DepositRequest(depositAmount);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
        when(userService.getUserByUsername(username)).thenReturn(mockUser);
        when(walletRepository.getWalletByUserId(userId)).thenReturn(mockWallet);

        walletService.depositToWallet(request);

        verify(walletRepository, times(1)).addFunds(depositAmount, walletId, userId);
        verify(emailService).sendEmailOnDeposit(
                "test@example.com",
                username,
                "EUR",
                "50.0",
                expectedNewBalance.toString());
    }

    @Test
    void testDepositToWallet_UserNotAuthenticated_ThrowsException() {
        DepositRequest request = new DepositRequest(BigDecimal.valueOf(50.00));

        when(securityContext.getAuthentication()).thenReturn(null);

        UserNotAuthenticatedException exception = assertThrows(
                UserNotAuthenticatedException.class,
                () -> walletService.depositToWallet(request));

        assertEquals("User was not authenticated! Try logging in", exception.getMessage());
        verify(walletRepository, never()).addFunds(any(), any(), any());
        verify(emailService, never()).sendEmailOnDeposit(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testDepositToWallet_UserWithNullEmail_DoesNotSendEmail() {
        String username = "testuser";
        Long userId = 1L;
        Long walletId = 10L;
        BigDecimal depositAmount = BigDecimal.valueOf(75.00);

        User mockUser = User.builder()
                .id(userId)
                .username(username)
                .email(null)
                .build();

        Wallet mockWallet = Wallet.builder()
                .id(walletId)
                .userId(userId)
                .currency(WalletCurrency.EUR)
                .balance(BigDecimal.valueOf(200.00))
                .build();

        DepositRequest request = new DepositRequest(depositAmount);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
        when(userService.getUserByUsername(username)).thenReturn(mockUser);
        when(walletRepository.getWalletByUserId(userId)).thenReturn(mockWallet);

        walletService.depositToWallet(request);

        verify(walletRepository, times(1)).addFunds(depositAmount, walletId, userId);
        verify(emailService, never()).sendEmailOnDeposit(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testDepositToWallet_LargeAmount() {
        String username = "testuser";
        Long userId = 1L;
        Long walletId = 10L;
        BigDecimal initialBalance = BigDecimal.valueOf(1000.00);
        BigDecimal depositAmount = BigDecimal.valueOf(10000.50);

        User mockUser = User.builder()
                .id(userId)
                .username(username)
                .email("test@example.com")
                .build();

        Wallet mockWallet = Wallet.builder()
                .id(walletId)
                .userId(userId)
                .currency(WalletCurrency.EUR)
                .balance(initialBalance)
                .build();

        DepositRequest request = new DepositRequest(depositAmount);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
        when(userService.getUserByUsername(username)).thenReturn(mockUser);
        when(walletRepository.getWalletByUserId(userId)).thenReturn(mockWallet);

        walletService.depositToWallet(request);

        verify(walletRepository, times(1)).addFunds(depositAmount, walletId, userId);
        verify(emailService).sendEmailOnDeposit(
                eq("test@example.com"),
                eq(username),
                eq("EUR"),
                eq("10000.5"),
                anyString());
    }
}