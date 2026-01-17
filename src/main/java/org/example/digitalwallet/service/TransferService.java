package org.example.digitalwallet.service;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.example.digitalwallet.dto.TransferRequest;
import org.example.digitalwallet.dto.TransferResponse;
import org.example.digitalwallet.exception.RateLimitExceededException;
import org.example.digitalwallet.exception.WalletNotFoundException;
import org.example.digitalwallet.model.Transfer;
import org.example.digitalwallet.model.Wallet;
import org.example.digitalwallet.repository.TransferRepository;
import org.example.digitalwallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransferService {

    private final TransferRepository transferRepository;
    private final WalletRepository walletRepository;


    public TransferService(TransferRepository transferRepository, WalletRepository walletRepository) {
        this.transferRepository = transferRepository;
        this.walletRepository = walletRepository;
    }

    @RateLimiter(name = "saveTransferRateLimiter" , fallbackMethod = "fallbackSaveTransfer")
    @Transactional
    public TransferResponse saveTransfer(TransferRequest transferRequest) {
        Long fromWalletId = transferRequest.getFromWallet();
        Long toWalletId = transferRequest.getToWallet();

        Wallet fromWallet = walletRepository.findById(fromWalletId);
        Wallet toWallet = walletRepository.findById(toWalletId);
        validateTransfer(fromWallet, toWallet, transferRequest);

        boolean success = walletRepository.executeTransfer(
                fromWalletId, toWalletId, transferRequest.getTransferAmount());

        if (!success) {
            throw new IllegalArgumentException("Insufficient funds: wallet balance is less than transfer amount");
        }

        Transfer transfer = Transfer.builder()
                .fromWallet(fromWalletId)
                .toWallet(toWalletId)
                .currency(transferRequest.getCurrency())
                .transferAmount(transferRequest.getTransferAmount())
                .transferDate(LocalDateTime.now())
                .build();

        transferRepository.save(transfer);

        return transferResponseMapper(transfer);
    }

    private void validateTransfer(Wallet fromWallet, Wallet toWallet, TransferRequest request) {
        if(fromWallet == null || toWallet == null) {
            throw new WalletNotFoundException("One of the wallets wasn't found or doesnt exist");
        }

        if(!fromWallet.getCurrency().equals(request.getCurrency())) {
            throw new IllegalArgumentException("Currency mismatch: wallet currency does not match transfer currency");
        }

        if(!toWallet.getCurrency().equals(request.getCurrency())) {
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
        return TransferResponse.builder()
                .id(transfer.getId())
                .fromWallet(transfer.getFromWallet())
                .toWallet(transfer.getToWallet())
                .currency(transfer.getCurrency())
                .transferAmount(transfer.getTransferAmount())
                .transferDate(transfer.getTransferDate())
                .build();
    }

    public TransferResponse fallbackSaveTransfer(TransferRequest transferRequest, Throwable throwable) {
        throw new RateLimitExceededException("Too many requests. Try again later!");
    }
}
