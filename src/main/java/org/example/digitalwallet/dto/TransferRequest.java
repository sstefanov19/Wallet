package org.example.digitalwallet.dto;

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
    private BigDecimal transferAmount;
}
