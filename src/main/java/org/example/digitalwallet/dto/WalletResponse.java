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
public class WalletResponse {
    private Long id;

    private Long userId;

    private WalletCurrency currency = WalletCurrency.EUR;

    private BigDecimal balance;

    private LocalDateTime createdDate;
}

