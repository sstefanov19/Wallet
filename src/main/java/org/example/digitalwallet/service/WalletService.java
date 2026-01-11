package org.example.digitalwallet.service;

import lombok.AllArgsConstructor;
import org.example.digitalwallet.dto.DepositRequest;
import org.example.digitalwallet.dto.WalletRequest;
import org.example.digitalwallet.exception.UserNotAuthenticatedException;
import org.example.digitalwallet.model.User;
import org.example.digitalwallet.model.Wallet;
import org.example.digitalwallet.model.WalletCurrency;
import org.example.digitalwallet.repository.UserRepository;
import org.example.digitalwallet.repository.WalletRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@Service
public class WalletService {


    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Transactional
    public void createWallet(WalletRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        WalletCurrency currency = request.getCurrency() != null
                ? request.getCurrency()
                : WalletCurrency.EUR;

        if(authentication == null) {
            throw new UserNotAuthenticatedException("User was not authenticated! Try logging in");
        }

        User user = userRepository.getUserByUsername(authentication.getName());

        Wallet wallet = Wallet.builder()
                .userId(user.getId())
                .currency(currency)
                .balance(request.getBalance())
                .createdDate(LocalDateTime.now())
                .build();

        walletRepository.createWallet(wallet);

        if (user.getEmail() != null) {
            emailService.sendWalletCreationEmail(
                    user.getEmail(),
                    authentication.getName(),
                    currency.name(),
                    wallet.getBalance().toString()
            );
        }
    }

    @Transactional
    public void depositToWallet(DepositRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null) {
            throw new UserNotAuthenticatedException("User was not authenticated! Try logging in");
        }

        User user = userRepository.getUserByUsername(authentication.getName());

        Wallet wallet = walletRepository.getWalletByUserId(user.getId());

        walletRepository.addFunds(request.getDepositAmount(), wallet.getId());

        BigDecimal newBalance = wallet.getBalance().add(request.getDepositAmount());

        if(user.getEmail() != null) {
        emailService.sendEmailOnDeposit(
                user.getEmail(),
                user.getUsername(),
                wallet.getCurrency().name(),
                request.getDepositAmount().toString(),
                newBalance.toString()
        );

        }
    }


}
