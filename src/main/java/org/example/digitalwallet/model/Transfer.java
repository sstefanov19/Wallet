package org.example.digitalwallet.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class Transfer {

    private Long id;

    private Long fromWallet;

    private Long toWallet;

    private WalletCurrency currency = WalletCurrency.EUR;

    private BigDecimal transferAmount;

    private LocalDateTime transferDate;
}
