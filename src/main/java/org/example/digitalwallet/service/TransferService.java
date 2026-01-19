package org.example.digitalwallet.service;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.example.digitalwallet.dto.TransferRequest;
import org.example.digitalwallet.dto.TransferResponse;
import org.example.digitalwallet.exception.RateLimitExceededException;
import org.example.digitalwallet.exception.UserNotAuthenticatedException;
import org.example.digitalwallet.exception.WalletNotFoundException;
import org.example.digitalwallet.model.Transfer;
import org.example.digitalwallet.model.User;
import org.example.digitalwallet.model.Wallet;
import org.example.digitalwallet.repository.TransferRepository;
import org.example.digitalwallet.repository.UserRepository;
import org.example.digitalwallet.repository.WalletRepository;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransferService {

    private final TransferRepository transferRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    public TransferService(TransferRepository transferRepository, WalletRepository walletRepository, UserRepository userRepository) {
        this.transferRepository = transferRepository;
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
    }

    @RateLimiter(name = "saveTransferRateLimiter" , fallbackMethod = "fallbackSaveTransfer")
    @Retryable(
            retryFor = {org.springframework.dao.DeadlockLoserDataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    @Transactional
    public TransferResponse saveTransfer(TransferRequest transferRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new UserNotAuthenticatedException("User was not authenticated! Try logging in");
        }

        User user = userRepository.getUserByUsername(authentication.getName());

        Long fromWalletId = transferRequest.fromWallet();
        Long toWalletId = transferRequest.toWallet();

        Wallet fromWallet = walletRepository.findById(fromWalletId);
        Wallet toWallet = walletRepository.findById(toWalletId);

        if (fromWallet == null || !fromWallet.getUserId().equals(user.getId())) {
            throw new SecurityException("You don't have permission to transfer from this wallet");
        }

        validateTransfer(fromWallet, toWallet, transferRequest);

        boolean success = walletRepository.executeTransfer(
                fromWalletId, toWalletId, transferRequest.transferAmount());

        if (!success) {
            throw new IllegalArgumentException("Insufficient funds: wallet balance is less than transfer amount");
        }

        Transfer transfer = Transfer.builder()
                .fromWallet(fromWalletId)
                .toWallet(toWalletId)
                .currency(transferRequest.currency())
                .transferAmount(transferRequest.transferAmount())
                .transferDate(LocalDateTime.now())
                .build();

        transferRepository.save(transfer);

        return transferResponseMapper(transfer);
    }

    private void validateTransfer(Wallet fromWallet, Wallet toWallet, TransferRequest request) {
        if(fromWallet == null || toWallet == null) {
            throw new WalletNotFoundException("One of the wallets wasn't found or doesnt exist");
        }

        if(!fromWallet.getCurrency().equals(request.currency())) {
            throw new IllegalArgumentException("Currency mismatch: wallet currency does not match transfer currency");
        }

        if(!toWallet.getCurrency().equals(request.currency())) {
            throw new IllegalArgumentException("Currency mismatch: recipient wallet currency does not match transfer currency");
        }
    }


    public List<TransferResponse> getTransferHistory(Long cursor,Integer limit) {
        List<Transfer> getTransfers = transferRepository.findTransfers(cursor, limit);

        if(getTransfers == null) {
            throw new RuntimeException("No transfers available for user");
        }

        return getTransfers.stream().map(this::transferResponseMapper).toList();
    }


    private TransferResponse transferResponseMapper(Transfer transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getFromWallet(),
                transfer.getToWallet(),
                transfer.getCurrency(),
                transfer.getTransferAmount(),
                transfer.getTransferDate()
        );
    }

    public TransferResponse fallbackSaveTransfer(TransferRequest transferRequest, Throwable throwable) {
        throw new RateLimitExceededException("Too many requests. Try again later!");
    }
}
