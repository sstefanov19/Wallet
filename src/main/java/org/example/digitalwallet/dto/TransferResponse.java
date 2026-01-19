package org.example.digitalwallet.dto;

import org.example.digitalwallet.model.WalletCurrency;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferResponse(
        Long id,
        Long fromWallet,
        Long toWallet,
        WalletCurrency currency,
        BigDecimal transferAmount,
        LocalDateTime transferDate
) {}