package org.example.digitalwallet.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.digitalwallet.model.WalletCurrency;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
public class TransferResponse {
    private Long id;

    private Long fromWallet;

    private Long toWallet;

    private WalletCurrency currency = WalletCurrency.EUR;

    private BigDecimal transferAmount;

    private LocalDateTime transferDate;
}
