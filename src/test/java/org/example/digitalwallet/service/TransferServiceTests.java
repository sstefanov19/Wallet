package org.example.digitalwallet.service;

import org.example.digitalwallet.dto.TransferRequest;
import org.example.digitalwallet.dto.TransferResponse;
import org.example.digitalwallet.exception.WalletNotFoundException;
import org.example.digitalwallet.model.Transfer;
import org.example.digitalwallet.model.Wallet;
import org.example.digitalwallet.model.WalletCurrency;
import org.example.digitalwallet.repository.TransferRepository;
import org.example.digitalwallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransferServiceTests {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private TransferService transferService;

    private static Wallet createWallet(Long id, Long userId, BigDecimal balance) {
        return Wallet.builder()
                .id(id)
                .userId(userId)
                .currency(WalletCurrency.EUR)
                .balance(balance)
                .build();
    }

    private static TransferRequest createTransferRequest(Long from, Long to, BigDecimal amount) {
        return new TransferRequest(from, to, WalletCurrency.EUR, amount);
    }

    // ========== Successful Transfer Tests ==========

    @Test
    void testTransfer_Success() {
        Wallet source = createWallet(1L, 100L, BigDecimal.valueOf(500.00));
        Wallet destination = createWallet(2L, 200L, BigDecimal.valueOf(100.00));
        TransferRequest request = createTransferRequest(1L, 2L, BigDecimal.valueOf(50.00));

        when(walletRepository.findById(1L)).thenReturn(source);
        when(walletRepository.findById(2L)).thenReturn(destination);
        when(walletRepository.deductFunds(BigDecimal.valueOf(50.00), 1L)).thenReturn(true);

        TransferResponse response = transferService.saveTransfer(request);

        assertEquals(1L, response.fromWallet());
        assertEquals(2L, response.toWallet());
        assertEquals(BigDecimal.valueOf(50.00), response.transferAmount());
        assertNotNull(response.transferDate());

        verify(walletRepository).deductFunds(BigDecimal.valueOf(50.00), 1L);
        verify(walletRepository).addFunds(BigDecimal.valueOf(50.00), 2L);
        verify(transferRepository).save(any(Transfer.class));
    }

    @Test
    void testTransfer_ExactBalance() {
        Wallet source = createWallet(1L, 100L, BigDecimal.valueOf(100.00));
        Wallet destination = createWallet(2L, 200L, BigDecimal.valueOf(50.00));
        TransferRequest request = createTransferRequest(1L, 2L, BigDecimal.valueOf(100.00));

        when(walletRepository.findById(1L)).thenReturn(source);
        when(walletRepository.findById(2L)).thenReturn(destination);
        when(walletRepository.deductFunds(BigDecimal.valueOf(100.00), 1L)).thenReturn(true);

        TransferResponse response = transferService.saveTransfer(request);

        assertEquals(BigDecimal.valueOf(100.00), response.transferAmount());
        verify(walletRepository).deductFunds(BigDecimal.valueOf(100.00), 1L);
        verify(walletRepository).addFunds(BigDecimal.valueOf(100.00), 2L);
    }

    @Test
    void testTransfer_HighPrecisionAmount() {
        BigDecimal preciseAmount = new BigDecimal("123.456789");
        Wallet source = createWallet(1L, 100L, BigDecimal.valueOf(500.00));
        Wallet destination = createWallet(2L, 200L, BigDecimal.valueOf(100.00));
        TransferRequest request = createTransferRequest(1L, 2L, preciseAmount);

        when(walletRepository.findById(1L)).thenReturn(source);
        when(walletRepository.findById(2L)).thenReturn(destination);
        when(walletRepository.deductFunds(preciseAmount, 1L)).thenReturn(true);

        TransferResponse response = transferService.saveTransfer(request);

        assertEquals(preciseAmount, response.transferAmount());
        verify(walletRepository).deductFunds(preciseAmount, 1L);
        verify(walletRepository).addFunds(preciseAmount, 2L);
    }

    @Test
    void testTransfer_LargeAmount() {
        BigDecimal largeAmount = new BigDecimal("9999999999.99");
        Wallet source = createWallet(1L, 100L, new BigDecimal("10000000000.00"));
        Wallet destination = createWallet(2L, 200L, BigDecimal.ZERO);
        TransferRequest request = createTransferRequest(1L, 2L, largeAmount);

        when(walletRepository.findById(1L)).thenReturn(source);
        when(walletRepository.findById(2L)).thenReturn(destination);
        when(walletRepository.deductFunds(largeAmount, 1L)).thenReturn(true);

        TransferResponse response = transferService.saveTransfer(request);

        assertEquals(largeAmount, response.transferAmount());
        verify(walletRepository).deductFunds(largeAmount, 1L);
        verify(walletRepository).addFunds(largeAmount, 2L);
    }

    @Test
    void testTransfer_InsufficientFunds_ShouldNotAddToRecipient() {
        Wallet source = createWallet(1L, 100L, BigDecimal.valueOf(500.00));
        Wallet destination = createWallet(2L, 200L, BigDecimal.valueOf(100.00));
        TransferRequest request = createTransferRequest(1L, 2L, BigDecimal.valueOf(1000.00));

        when(walletRepository.findById(1L)).thenReturn(source);
        when(walletRepository.findById(2L)).thenReturn(destination);
        when(walletRepository.deductFunds(BigDecimal.valueOf(1000.00), 1L)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transferService.saveTransfer(request));

        assertEquals("Insufficient funds: wallet balance is less than transfer amount", exception.getMessage());
        verify(walletRepository).deductFunds(BigDecimal.valueOf(1000.00), 1L);
        verify(walletRepository, never()).addFunds(any(), any());
        verify(transferRepository, never()).save(any());
    }

    @Test
    void testTransfer_BalanceOneCentLess() {
        Wallet source = createWallet(1L, 100L, BigDecimal.valueOf(99.99));
        Wallet destination = createWallet(2L, 200L, BigDecimal.valueOf(50.00));
        TransferRequest request = createTransferRequest(1L, 2L, BigDecimal.valueOf(100.00));

        when(walletRepository.findById(1L)).thenReturn(source);
        when(walletRepository.findById(2L)).thenReturn(destination);
        when(walletRepository.deductFunds(BigDecimal.valueOf(100.00), 1L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> transferService.saveTransfer(request));

        verify(walletRepository, never()).addFunds(any(), any());
        verify(transferRepository, never()).save(any());
    }

    @Test
    void testTransfer_ZeroBalance() {
        Wallet source = createWallet(1L, 100L, BigDecimal.ZERO);
        Wallet destination = createWallet(2L, 200L, BigDecimal.valueOf(100.00));
        TransferRequest request = createTransferRequest(1L, 2L, BigDecimal.valueOf(50.00));

        when(walletRepository.findById(1L)).thenReturn(source);
        when(walletRepository.findById(2L)).thenReturn(destination);
        when(walletRepository.deductFunds(any(), eq(1L))).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> transferService.saveTransfer(request));

        verify(walletRepository, never()).addFunds(any(), any());
    }

    @Test
    void testTransfer_SourceWalletNotFound() {
        Wallet destination = createWallet(2L, 200L, BigDecimal.valueOf(100.00));
        TransferRequest request = createTransferRequest(1L, 2L, BigDecimal.valueOf(50.00));

        when(walletRepository.findById(1L)).thenReturn(null);
        when(walletRepository.findById(2L)).thenReturn(destination);

        WalletNotFoundException exception = assertThrows(
                WalletNotFoundException.class,
                () -> transferService.saveTransfer(request));

        assertEquals("One of the wallets wasn't found or doesnt exist", exception.getMessage());
        verify(walletRepository, never()).deductFunds(any(), any());
        verify(walletRepository, never()).addFunds(any(), any());
        verify(transferRepository, never()).save(any());
    }

    @Test
    void testTransfer_DestinationWalletNotFound() {
        Wallet source = createWallet(1L, 100L, BigDecimal.valueOf(500.00));
        TransferRequest request = createTransferRequest(1L, 2L, BigDecimal.valueOf(50.00));

        when(walletRepository.findById(1L)).thenReturn(source);
        when(walletRepository.findById(2L)).thenReturn(null);

        WalletNotFoundException exception = assertThrows(
                WalletNotFoundException.class,
                () -> transferService.saveTransfer(request));

        assertEquals("One of the wallets wasn't found or doesnt exist", exception.getMessage());
        verify(walletRepository, never()).deductFunds(any(), any());
        verify(walletRepository, never()).addFunds(any(), any());
    }

    @Test
    void testTransfer_BothWalletsNotFound() {
        TransferRequest request = createTransferRequest(1L, 2L, BigDecimal.valueOf(50.00));

        when(walletRepository.findById(1L)).thenReturn(null);
        when(walletRepository.findById(2L)).thenReturn(null);

        assertThrows(WalletNotFoundException.class, () -> transferService.saveTransfer(request));

        verify(walletRepository, never()).deductFunds(any(), any());
        verify(walletRepository, never()).addFunds(any(), any());
    }

    @Test
    void testTransfer_DeductAndAddSameAmount() {
        BigDecimal amount = new BigDecimal("123.45");
        Wallet source = createWallet(1L, 100L, BigDecimal.valueOf(500.00));
        Wallet destination = createWallet(2L, 200L, BigDecimal.valueOf(100.00));
        TransferRequest request = createTransferRequest(1L, 2L, amount);

        when(walletRepository.findById(1L)).thenReturn(source);
        when(walletRepository.findById(2L)).thenReturn(destination);
        when(walletRepository.deductFunds(amount, 1L)).thenReturn(true);

        transferService.saveTransfer(request);

        ArgumentCaptor<BigDecimal> deductCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        ArgumentCaptor<BigDecimal> addCaptor = ArgumentCaptor.forClass(BigDecimal.class);

        verify(walletRepository).deductFunds(deductCaptor.capture(), eq(1L));
        verify(walletRepository).addFunds(addCaptor.capture(), eq(2L));

        assertEquals(deductCaptor.getValue(), addCaptor.getValue(),
                "Deducted amount must equal added amount to prevent money loss/creation");
    }

    @Test
    void testTransfer_RecordsCorrectAmount() {
        BigDecimal transferAmount = BigDecimal.valueOf(75.50);
        Wallet source = createWallet(1L, 100L, BigDecimal.valueOf(500.00));
        Wallet destination = createWallet(2L, 200L, BigDecimal.valueOf(100.00));
        TransferRequest request = createTransferRequest(1L, 2L, transferAmount);

        when(walletRepository.findById(1L)).thenReturn(source);
        when(walletRepository.findById(2L)).thenReturn(destination);
        when(walletRepository.deductFunds(transferAmount, 1L)).thenReturn(true);

        transferService.saveTransfer(request);

        ArgumentCaptor<Transfer> transferCaptor = ArgumentCaptor.forClass(Transfer.class);
        verify(transferRepository).save(transferCaptor.capture());

        Transfer savedTransfer = transferCaptor.getValue();
        assertEquals(transferAmount, savedTransfer.getTransferAmount());
        assertEquals(1L, savedTransfer.getFromWallet());
        assertEquals(2L, savedTransfer.getToWallet());
    }

    @Test
    void testTransfer_RecordCreatedOnlyOnSuccess() {
        Wallet source = createWallet(1L, 100L, BigDecimal.valueOf(50.00));
        Wallet destination = createWallet(2L, 200L, BigDecimal.valueOf(100.00));
        TransferRequest request = createTransferRequest(1L, 2L, BigDecimal.valueOf(100.00));

        when(walletRepository.findById(1L)).thenReturn(source);
        when(walletRepository.findById(2L)).thenReturn(destination);
        when(walletRepository.deductFunds(any(), eq(1L))).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> transferService.saveTransfer(request));

        verify(transferRepository, never()).save(any());
    }

    @Test
    void testTransfer_OneCent() {
        BigDecimal oneCent = new BigDecimal("0.01");
        Wallet source = createWallet(1L, 100L, BigDecimal.valueOf(100.00));
        Wallet destination = createWallet(2L, 200L, BigDecimal.valueOf(50.00));
        TransferRequest request = createTransferRequest(1L, 2L, oneCent);

        when(walletRepository.findById(1L)).thenReturn(source);
        when(walletRepository.findById(2L)).thenReturn(destination);
        when(walletRepository.deductFunds(oneCent, 1L)).thenReturn(true);

        TransferResponse response = transferService.saveTransfer(request);

        assertEquals(oneCent, response.transferAmount());
        verify(walletRepository).deductFunds(oneCent, 1L);
        verify(walletRepository).addFunds(oneCent, 2L);
    }

    @Test
    void testTransfer_PreserveScale() {
        BigDecimal amount = new BigDecimal("100.00");
        Wallet source = createWallet(1L, 100L, BigDecimal.valueOf(500.00));
        Wallet destination = createWallet(2L, 200L, BigDecimal.valueOf(100.00));
        TransferRequest request = createTransferRequest(1L, 2L, amount);

        when(walletRepository.findById(1L)).thenReturn(source);
        when(walletRepository.findById(2L)).thenReturn(destination);
        when(walletRepository.deductFunds(amount, 1L)).thenReturn(true);

        transferService.saveTransfer(request);

        ArgumentCaptor<BigDecimal> captor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(walletRepository).addFunds(captor.capture(), eq(2L));

        assertEquals(amount.scale(), captor.getValue().scale(),
                "Scale should be preserved to avoid rounding issues");
    }

    @Test
    void testTransfer_ManyDecimalPlaces() {
        BigDecimal preciseAmount = new BigDecimal("0.123456789012345");
        Wallet source = createWallet(1L, 100L, BigDecimal.valueOf(100.00));
        Wallet destination = createWallet(2L, 200L, BigDecimal.valueOf(50.00));
        TransferRequest request = createTransferRequest(1L, 2L, preciseAmount);

        when(walletRepository.findById(1L)).thenReturn(source);
        when(walletRepository.findById(2L)).thenReturn(destination);
        when(walletRepository.deductFunds(preciseAmount, 1L)).thenReturn(true);

        TransferResponse response = transferService.saveTransfer(request);

        assertEquals(preciseAmount, response.transferAmount());
    }

    @Test
    void testTransfer_SameWallet_IsAllowed() {
        Wallet wallet = createWallet(1L, 100L, BigDecimal.valueOf(500.00));
        TransferRequest request = createTransferRequest(1L, 1L, BigDecimal.valueOf(50.00));

        when(walletRepository.findById(1L)).thenReturn(wallet);
        when(walletRepository.deductFunds(any(), eq(1L))).thenReturn(true);

        TransferResponse response = transferService.saveTransfer(request);

        assertNotNull(response);
        assertEquals(1L, response.fromWallet());
        assertEquals(1L, response.toWallet());
    }

    @Test
    void testTransfer_DeductBeforeAdd() {
        Wallet source = createWallet(1L, 100L, BigDecimal.valueOf(500.00));
        Wallet destination = createWallet(2L, 200L, BigDecimal.valueOf(100.00));
        TransferRequest request = createTransferRequest(1L, 2L, BigDecimal.valueOf(50.00));

        when(walletRepository.findById(1L)).thenReturn(source);
        when(walletRepository.findById(2L)).thenReturn(destination);
        when(walletRepository.deductFunds(any(), eq(1L))).thenReturn(true);

        transferService.saveTransfer(request);

        var inOrder = inOrder(walletRepository, transferRepository);
        inOrder.verify(walletRepository).findById(1L);
        inOrder.verify(walletRepository).findById(2L);
        inOrder.verify(walletRepository).deductFunds(any(), eq(1L));
        inOrder.verify(walletRepository).addFunds(any(), eq(2L));
        inOrder.verify(transferRepository).save(any());
    }

    @Test
    void testTransfer_ValidateBeforeMovement() {
        TransferRequest request = createTransferRequest(1L, 2L, BigDecimal.valueOf(50.00));

        when(walletRepository.findById(1L)).thenReturn(null);
        when(walletRepository.findById(2L)).thenReturn(createWallet(2L, 200L, BigDecimal.valueOf(100.00)));

        assertThrows(WalletNotFoundException.class, () -> transferService.saveTransfer(request));

        verify(walletRepository, never()).deductFunds(any(), any());
        verify(walletRepository, never()).addFunds(any(), any());
    }
}
