package org.example.digitalwallet.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.example.digitalwallet.model.WalletCurrency;

import java.math.BigDecimal;

public record WalletRequest(
        WalletCurrency currency,

        @NotNull(message = "Balance is required")
        @PositiveOrZero(message = "Starting balance must be zero or positive")
        BigDecimal balance
) {}