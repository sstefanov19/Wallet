package org.example.digitalwallet.service;

import org.example.digitalwallet.dto.TransferRequest;
import org.example.digitalwallet.dto.TransferResponse;
import org.example.digitalwallet.exception.RateLimitExceededException;
import org.example.digitalwallet.exception.UserNotAuthenticatedException;
import org.example.digitalwallet.exception.WalletNotFoundException;
import org.example.digitalwallet.model.Transfer;
import org.example.digitalwallet.model.WalletCurrency;
import org.example.digitalwallet.repository.TransferRepository;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransferServiceTests {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private WalletService walletService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private TransferService transferService;

    private static final String USERNAME = "testuser";

    private static TransferRequest createRequest(Long from, Long to, BigDecimal amount) {
        return new TransferRequest(from, to, WalletCurrency.EUR, amount);
    }

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn(USERNAME);
    }

    // ========== saveTransfer Tests ==========

    @Test
    void testSaveTransfer_Success() {
        TransferRequest request = createRequest(1L, 2L, BigDecimal.valueOf(50.00));
        when(walletService.executeTransfer(1L, 2L, BigDecimal.valueOf(50.00), WalletCurrency.EUR, USERNAME))
                .thenReturn(true);

        TransferResponse response = transferService.saveTransfer(request);

        assertEquals(1L, response.fromWallet());
        assertEquals(2L, response.toWallet());
        assertEquals(BigDecimal.valueOf(50.00), response.transferAmount());
        assertNotNull(response.transferDate());
        verify(transferRepository).save(any(Transfer.class));
    }

    @Test
    void testSaveTransfer_UserNotAuthenticated_ThrowsException() {
        when(securityContext.getAuthentication()).thenReturn(null);
        TransferRequest request = createRequest(1L, 2L, BigDecimal.valueOf(50.00));

        assertThrows(UserNotAuthenticatedException.class, () -> transferService.saveTransfer(request));

        verify(walletService, never()).executeTransfer(any(), any(), any(), any(), any());
        verify(transferRepository, never()).save(any());
    }

    @Test
    void testSaveTransfer_InsufficientFunds_ThrowsException() {
        TransferRequest request = createRequest(1L, 2L, BigDecimal.valueOf(1000.00));
        when(walletService.executeTransfer(1L, 2L, BigDecimal.valueOf(1000.00), WalletCurrency.EUR, USERNAME))
                .thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transferService.saveTransfer(request));

        assertEquals("Insufficient funds: wallet balance is less than transfer amount", exception.getMessage());
        verify(transferRepository, never()).save(any());
    }

    @Test
    void testSaveTransfer_WalletNotFound_PropagatesException() {
        TransferRequest request = createRequest(1L, 2L, BigDecimal.valueOf(50.00));
        when(walletService.executeTransfer(any(), any(), any(), any(), any()))
                .thenThrow(new WalletNotFoundException("One of the wallets wasn't found or doesn't exist"));

        assertThrows(WalletNotFoundException.class, () -> transferService.saveTransfer(request));
        verify(transferRepository, never()).save(any());
    }

    @Test
    void testSaveTransfer_UnauthorizedWallet_PropagatesException() {
        TransferRequest request = createRequest(1L, 2L, BigDecimal.valueOf(50.00));
        when(walletService.executeTransfer(any(), any(), any(), any(), any()))
                .thenThrow(new SecurityException("You don't have permission to transfer from this wallet"));

        assertThrows(SecurityException.class, () -> transferService.saveTransfer(request));
        verify(transferRepository, never()).save(any());
    }

    @Test
    void testSaveTransfer_CurrencyMismatch_PropagatesException() {
        TransferRequest request = createRequest(1L, 2L, BigDecimal.valueOf(50.00));
        when(walletService.executeTransfer(any(), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Currency mismatch: source wallet currency does not match transfer currency"));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> transferService.saveTransfer(request));

        assertTrue(ex.getMessage().contains("Currency mismatch"));
        verify(transferRepository, never()).save(any());
    }

    @Test
    void testSaveTransfer_RecordsCorrectData() {
        BigDecimal amount = BigDecimal.valueOf(75.50);
        TransferRequest request = createRequest(1L, 2L, amount);
        when(walletService.executeTransfer(1L, 2L, amount, WalletCurrency.EUR, USERNAME)).thenReturn(true);

        transferService.saveTransfer(request);

        ArgumentCaptor<Transfer> captor = ArgumentCaptor.forClass(Transfer.class);
        verify(transferRepository).save(captor.capture());

        Transfer saved = captor.getValue();
        assertEquals(1L, saved.getFromWallet());
        assertEquals(2L, saved.getToWallet());
        assertEquals(amount, saved.getTransferAmount());
        assertEquals(WalletCurrency.EUR, saved.getCurrency());
        assertNotNull(saved.getTransferDate());
    }

    @Test
    void testSaveTransfer_TransferNotSavedOnFailure() {
        TransferRequest request = createRequest(1L, 2L, BigDecimal.valueOf(100.00));
        when(walletService.executeTransfer(any(), any(), any(), any(), any())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> transferService.saveTransfer(request));
        verify(transferRepository, never()).save(any());
    }

    @Test
    void testSaveTransfer_ExecuteCalledBeforeSave() {
        TransferRequest request = createRequest(1L, 2L, BigDecimal.valueOf(50.00));
        when(walletService.executeTransfer(any(), any(), any(), any(), any())).thenReturn(true);

        transferService.saveTransfer(request);

        var inOrder = inOrder(walletService, transferRepository);
        inOrder.verify(walletService).executeTransfer(any(), any(), any(), any(), any());
        inOrder.verify(transferRepository).save(any());
    }

    @Test
    void testSaveTransfer_OneCent() {
        BigDecimal oneCent = new BigDecimal("0.01");
        TransferRequest request = createRequest(1L, 2L, oneCent);
        when(walletService.executeTransfer(1L, 2L, oneCent, WalletCurrency.EUR, USERNAME)).thenReturn(true);

        TransferResponse response = transferService.saveTransfer(request);

        assertEquals(oneCent, response.transferAmount());
    }

    @Test
    void testSaveTransfer_LargeAmount() {
        BigDecimal largeAmount = new BigDecimal("9999999999.99");
        TransferRequest request = createRequest(1L, 2L, largeAmount);
        when(walletService.executeTransfer(1L, 2L, largeAmount, WalletCurrency.EUR, USERNAME)).thenReturn(true);

        TransferResponse response = transferService.saveTransfer(request);

        assertEquals(largeAmount, response.transferAmount());
    }

    @Test
    void testSaveTransfer_SameWallet() {
        TransferRequest request = createRequest(1L, 1L, BigDecimal.valueOf(50.00));
        when(walletService.executeTransfer(1L, 1L, BigDecimal.valueOf(50.00), WalletCurrency.EUR, USERNAME))
                .thenReturn(true);

        TransferResponse response = transferService.saveTransfer(request);

        assertEquals(1L, response.fromWallet());
        assertEquals(1L, response.toWallet());
    }

    // ========== getTransferHistory Tests ==========

    @Test
    void testGetTransferHistory_ReturnsResults() {
        Transfer t = Transfer.builder()
                .id(1L).fromWallet(1L).toWallet(2L)
                .currency(WalletCurrency.EUR)
                .transferAmount(BigDecimal.valueOf(50.00))
                .transferDate(java.time.LocalDateTime.now())
                .build();
        when(transferRepository.findTransfers(null, 10)).thenReturn(List.of(t));

        List<TransferResponse> results = transferService.getTransferHistory(null, 10);

        assertEquals(1, results.size());
        assertEquals(1L, results.getFirst().fromWallet());
    }

    @Test
    void testGetTransferHistory_EmptyList_ReturnsEmpty() {
        when(transferRepository.findTransfers(null, 10)).thenReturn(List.of());

        List<TransferResponse> results = transferService.getTransferHistory(null, 10);

        assertTrue(results.isEmpty());
    }

    @Test
    void testGetTransferHistory_WithCursor() {
        when(transferRepository.findTransfers(5L, 10)).thenReturn(List.of());

        transferService.getTransferHistory(5L, 10);

        verify(transferRepository).findTransfers(5L, 10);
    }
}
