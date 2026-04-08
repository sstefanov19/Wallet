package org.example.digitalwallet.service;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.AllArgsConstructor;
import org.example.digitalwallet.dto.TransferRequest;
import org.example.digitalwallet.dto.TransferResponse;
import org.example.digitalwallet.exception.RateLimitExceededException;
import org.example.digitalwallet.exception.UserNotAuthenticatedException;
import org.example.digitalwallet.model.Transfer;
import org.example.digitalwallet.repository.TransferRepository;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Service
public class TransferService {

    private final TransferRepository transferRepository;
    private final WalletService walletService;

    @RateLimiter(name = "saveTransferRateLimiter" , fallbackMethod="fallbackSaveTransfer")
    @Retryable(
            retryFor = {
                DeadlockLoserDataAccessException.class,
                CannotAcquireLockException.class
            },
            maxAttempts = 5,
            backoff = @Backoff(delay = 100, maxDelay = 1000, multiplier = 2, random = true)
    )
    @Transactional
    public TransferResponse saveTransfer(TransferRequest transferRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new UserNotAuthenticatedException("User was not authenticated! Try logging in");
        }

        Long fromWalletId = transferRequest.fromWallet();
        Long toWalletId = transferRequest.toWallet();

        boolean success = walletService.executeTransfer(
                fromWalletId, toWalletId, transferRequest.transferAmount(),
                transferRequest.currency(), authentication.getName());

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


    public List<TransferResponse> getTransferHistory(Long cursor,Integer limit) {
        List<Transfer> getTransfers = transferRepository.findTransfers(cursor, limit);

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

    public TransferResponse fallbackSaveTransfer(TransferRequest transferRequest, RequestNotPermitted ex) {
        throw new RateLimitExceededException("Too many requests. Try again later!");
    }
}
