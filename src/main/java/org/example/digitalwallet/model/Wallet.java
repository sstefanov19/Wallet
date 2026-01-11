package org.example.digitalwallet.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Wallet {

    private Long id;

    private Long userId;

    private WalletCurrency currency = WalletCurrency.EUR;

    private BigDecimal balance;

    private LocalDateTime createdDate;
}
