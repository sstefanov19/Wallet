package org.example.digitalwallet.service;

import lombok.AllArgsConstructor;
import org.example.digitalwallet.dto.DepositRequest;
import org.example.digitalwallet.dto.WalletRequest;
import org.example.digitalwallet.dto.WalletResponse;
import org.example.digitalwallet.exception.UserNotAuthenticatedException;
import org.example.digitalwallet.exception.WalletNotFoundException;
import org.example.digitalwallet.model.User;
import org.example.digitalwallet.model.Wallet;
import org.example.digitalwallet.model.WalletCurrency;
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
    private final UserService userService;
    private final EmailService emailService;

    @Transactional
    public void createWallet(WalletRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null) {
            throw new UserNotAuthenticatedException("User was not authenticated! Try logging in");
        }

        WalletCurrency currency = request.currency() != null
                ? request.currency()
                : WalletCurrency.EUR;


        User user = userService.getUserByUsername(authentication.getName());

        Wallet wallet = Wallet.builder()
                .userId(user.getId())
                .currency(currency)
                .balance(request.balance())
                .createdAt(LocalDateTime.now())
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

        User user = userService.getUserByUsername(authentication.getName());

        Wallet wallet = walletRepository.getWalletByUserId(user.getId());

        walletRepository.addFunds(request.depositAmount(), wallet.getId(), user.getId());

        BigDecimal newBalance = wallet.getBalance().add(request.depositAmount());

        if(user.getEmail() != null) {
        emailService.sendEmailOnDeposit(
                user.getEmail(),
                user.getUsername(),
                wallet.getCurrency().name(),
                request.depositAmount().toString(),
                newBalance.toString()
        );

        }
    }


    public WalletResponse getWalletById(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new UserNotAuthenticatedException("User was not authenticated! Try logging in");
        }

        User user = userService.getUserByUsername(authentication.getName());

        Wallet foundWallet = walletRepository.findById(id);

        if (foundWallet == null) {
            throw new WalletNotFoundException("Wallet wasn't found!");
        }

        if (!foundWallet.getUserId().equals(user.getId())) {
            throw new SecurityException("You don't have access to this wallet");
        }

        return new WalletResponse(
                foundWallet.getId(),
                foundWallet.getUserId(),
                foundWallet.getCurrency(),
                foundWallet.getBalance(),
                foundWallet.getCreatedAt()
        );
    }

    public boolean executeTransfer(Long fromWalletId, Long toWalletId, BigDecimal amount, WalletCurrency currency, String callerUsername) {
        User caller = userService.getUserByUsername(callerUsername);

        Wallet fromWallet = walletRepository.findById(fromWalletId);
        Wallet toWallet = walletRepository.findById(toWalletId);

        if (fromWallet == null || toWallet == null) {
            throw new WalletNotFoundException("One of the wallets wasn't found or doesn't exist");
        }

        if (!fromWallet.getUserId().equals(caller.getId())) {
            throw new SecurityException("You don't have permission to transfer from this wallet");
        }

        if (!fromWallet.getCurrency().equals(currency)) {
            throw new IllegalArgumentException("Currency mismatch: source wallet currency does not match transfer currency");
        }
        if (!toWallet.getCurrency().equals(currency)) {
            throw new IllegalArgumentException("Currency mismatch: recipient wallet currency does not match transfer currency");
        }

        return walletRepository.executeTransfer(fromWalletId, toWalletId, amount);
    }


}
