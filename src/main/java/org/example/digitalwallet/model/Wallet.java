package org.example.digitalwallet.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Wallet {

    private Integer id;

    private Integer userId;

    private WalletCurrency currency;

    private BigDecimal balance;

    private LocalDateTime createdDate;

}
