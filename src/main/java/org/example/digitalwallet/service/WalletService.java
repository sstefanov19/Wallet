package org.example.digitalwallet.service;

import org.example.digitalwallet.dto.WalletRequest;
import org.example.digitalwallet.exception.UserNotAuthenticatedException;
import org.example.digitalwallet.model.Wallet;
import org.example.digitalwallet.model.WalletCurrency;
import org.example.digitalwallet.repository.UserRepository;
import org.example.digitalwallet.repository.WalletRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class WalletService {


    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    public WalletService(WalletRepository walletRepository, UserRepository userRepository) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void createWallet(WalletRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        WalletCurrency currency = request.getCurrency() != null
                ? request.getCurrency()
                : WalletCurrency.EUR;

        if(authentication == null) {
            throw new UserNotAuthenticatedException("User was not authenticated! Try logging in");
        }

        Long userId = userRepository.getUserIdByName(authentication.getName());

        Wallet wallet = Wallet.builder()
                .userId(userId)
                .currency(currency)
                .balance(request.getBalance())
                .createdDate(LocalDateTime.now())
                .build();

        walletRepository.createWallet(wallet);
    }


}
