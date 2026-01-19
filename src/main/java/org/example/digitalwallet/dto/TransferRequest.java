package org.example.digitalwallet.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.example.digitalwallet.model.WalletCurrency;

import java.math.BigDecimal;

@Getter
@Setter
public class TransferRequest {

    private Long fromWallet;
    private Long toWallet;
    private WalletCurrency currency = WalletCurrency.EUR;

    @Positive(message = "Value must be positive")
    @DecimalMax(value = "100000" ,message = "Transfer limit exceeded")
    @NotBlank(message = "Value cannot be blank")
    private BigDecimal transferAmount;
}
