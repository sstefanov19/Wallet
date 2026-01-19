package org.example.digitalwallet.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.example.digitalwallet.model.WalletCurrency;

import java.math.BigDecimal;

public record TransferRequest(
        @NotNull(message = "From wallet is required")
        Long fromWallet,

        @NotNull(message = "To wallet is required")
        Long toWallet,

        WalletCurrency currency,

        @NotNull(message = "Transfer amount is required")
        @Positive(message = "Value must be positive")
        @DecimalMax(value = "100000", message = "Transfer limit exceeded")
        BigDecimal transferAmount
) {
    public TransferRequest {
        if (currency == null) {
            currency = WalletCurrency.EUR;
        }
    }
}