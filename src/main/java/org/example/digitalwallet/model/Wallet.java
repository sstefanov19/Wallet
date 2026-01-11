package org.example.digitalwallet.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class Wallet {

    private Long id;

    private Long userId;

    private WalletCurrency currency = WalletCurrency.EUR;

    private BigDecimal balance;

    private LocalDateTime createdDate;
}
