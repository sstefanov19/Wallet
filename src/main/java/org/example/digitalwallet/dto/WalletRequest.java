package org.example.digitalwallet.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.example.digitalwallet.model.WalletCurrency;

import java.math.BigDecimal;

public record WalletRequest(
        @NotNull(message = "You need to provide a currency")
        WalletCurrency currency,

        @NotNull(message = "Balance is required")
        @Positive(message = "Starting balance should be positive")
        BigDecimal balance
) {}