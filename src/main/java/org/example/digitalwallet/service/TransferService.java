package org.example.digitalwallet.service;

import org.example.digitalwallet.dto.TransferRequest;
import org.example.digitalwallet.dto.TransferResponse;
import org.example.digitalwallet.exception.WalletNotFoundException;
import org.example.digitalwallet.model.Transfer;
import org.example.digitalwallet.model.User;
import org.example.digitalwallet.model.Wallet;
import org.example.digitalwallet.repository.TransferRepository;
import org.example.digitalwallet.repository.UserRepository;
import org.example.digitalwallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TransferService {

    private final TransferRepository transferRepository;
    private final WalletRepository walletRepository;


    public TransferService(TransferRepository transferRepository, WalletRepository walletRepository) {
        this.transferRepository = transferRepository;
        this.walletRepository = walletRepository;
    }

    @Transactional
    public TransferResponse saveTransfer(TransferRequest transferRequest) {
        Wallet fromWallet = walletRepository.findById(transferRequest.getFromWallet());
        Wallet toWallet = walletRepository.findById(transferRequest.getToWallet());

        validateTransfer(fromWallet, toWallet , transferRequest);

        walletRepository.deductFunds(transferRequest.getTransferAmount(), fromWallet.getId());
        walletRepository.addFunds(transferRequest.getTransferAmount(), toWallet.getId());

        Transfer transfer = Transfer.builder()
                .fromWallet(transferRequest.getFromWallet())
                .toWallet(transferRequest.getToWallet())
                .currency(transferRequest.getCurrency())
                .transferAmount(transferRequest.getTransferAmount())
                .transferDate(LocalDateTime.now())
                .build();


        transferRepository.save(transfer);

        return TransferResponse.builder()
                .fromWallet(transfer.getFromWallet())
                .toWallet(transfer.getToWallet())
                .currency(transfer.getCurrency())
                .transferAmount(transfer.getTransferAmount())
                .transferDate(transfer.getTransferDate())
                .build();
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

        if(fromWallet.getBalance().compareTo(request.getTransferAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient funds: wallet balance is less than transfer amount");
        }

    }


}
