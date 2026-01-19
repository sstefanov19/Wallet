package org.example.digitalwallet.dto;

import org.example.digitalwallet.model.WalletCurrency;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletResponse(
        Long id,
        Long userId,
        WalletCurrency currency,
        BigDecimal balance,
        LocalDateTime createdDate
) {}