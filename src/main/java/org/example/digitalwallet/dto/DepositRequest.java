package org.example.digitalwallet.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record DepositRequest(
        @NotNull(message = "Deposit amount is required")
        @Positive(message = "Value must be positive")
        @DecimalMax(value = "100000", message = "Deposit limit exceeded")
        BigDecimal depositAmount
) {}