package org.example.digitalwallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.digitalwallet.dto.DepositRequest;
import org.example.digitalwallet.dto.WalletRequest;
import org.example.digitalwallet.model.WalletCurrency;
import org.example.digitalwallet.service.CustomUserDetailService;
import org.example.digitalwallet.service.WalletService;
import org.example.digitalwallet.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WalletController.class)
@AutoConfigureMockMvc(addFilters = false)
public class WalletControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private WalletService walletService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailService customUserDetailService;

    // ========== Create Wallet Tests ==========

    @Test
    @WithMockUser
    void testCreateWallet_Success() throws Exception {
        // Arrange
        WalletRequest request = new WalletRequest(WalletCurrency.EUR, BigDecimal.valueOf(100));

        doNothing().when(walletService).createWallet(any(WalletRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/v1/wallet/create")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Created new wallet"));

        verify(walletService, times(1)).createWallet(any(WalletRequest.class));
    }

    @Test
    @WithMockUser
    void testCreateWallet_WithNullCurrency() throws Exception {
        // Arrange
        WalletRequest request = new WalletRequest(null, BigDecimal.valueOf(50));

        doNothing().when(walletService).createWallet(any(WalletRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/v1/wallet/create")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Created new wallet"));

        verify(walletService, times(1)).createWallet(any(WalletRequest.class));
    }

    @Test
    @WithMockUser
    void testCreateWallet_WithZeroBalance() throws Exception {
        // Arrange
        WalletRequest request = new WalletRequest(WalletCurrency.EUR, BigDecimal.ZERO);

        doNothing().when(walletService).createWallet(any(WalletRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/v1/wallet/create")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Created new wallet"));

        verify(walletService, times(1)).createWallet(any(WalletRequest.class));
    }

    // ========== Deposit to Wallet Tests ==========

    @Test
    @WithMockUser
    void testDepositToWallet_Success() throws Exception {
        // Arrange
        DepositRequest request = new DepositRequest(BigDecimal.valueOf(50.00));

        doNothing().when(walletService).depositToWallet(any(DepositRequest.class));

        // Act & Assert
        mockMvc.perform(put("/api/v1/wallet/deposit")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Deposit of 50.0 was successful "));

        verify(walletService, times(1)).depositToWallet(any(DepositRequest.class));
    }

    @Test
    @WithMockUser
    void testDepositToWallet_LargeAmount() throws Exception {
        // Arrange
        DepositRequest request = new DepositRequest(BigDecimal.valueOf(10000.50));

        doNothing().when(walletService).depositToWallet(any(DepositRequest.class));

        // Act & Assert
        mockMvc.perform(put("/api/v1/wallet/deposit")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Deposit of 10000.5 was successful "));

        verify(walletService, times(1)).depositToWallet(any(DepositRequest.class));
    }

    @Test
    @WithMockUser
    void testDepositToWallet_SmallAmount() throws Exception {
        // Arrange
        DepositRequest request = new DepositRequest(BigDecimal.valueOf(0.01));

        doNothing().when(walletService).depositToWallet(any(DepositRequest.class));

        // Act & Assert
        mockMvc.perform(put("/api/v1/wallet/deposit")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Deposit of 0.01 was successful "));

        verify(walletService, times(1)).depositToWallet(any(DepositRequest.class));
    }

    @Test
    @WithMockUser
    void testDepositToWallet_InvalidJson() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/v1/wallet/deposit")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());

        verify(walletService, never()).depositToWallet(any(DepositRequest.class));
    }
}
