package org.example.digitalwallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.example.digitalwallet.model.WalletCurrency;

import java.math.BigDecimal;


@Getter
@Setter
public class WalletRequest {

    @NotBlank(message = "You need to provide a currency")
    private WalletCurrency currency;

    @Positive(message = "Starting balance should be positive")
    @NotBlank(message = "Balance cannot be blank")
    private BigDecimal balance;
}
