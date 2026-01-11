package org.example.digitalwallet.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.digitalwallet.model.WalletCurrency;

import java.math.BigDecimal;


@Getter
@Setter
public class WalletRequest {
    private WalletCurrency currency;
    private BigDecimal balance;
}
