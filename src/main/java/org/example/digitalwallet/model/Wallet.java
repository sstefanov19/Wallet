package org.example.digitalwallet.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Wallet {

    private Long id;

    private Long userId;

    private WalletCurrency currency;

    private BigDecimal balance;

    private LocalDateTime createdDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public WalletCurrency getCurrency() {
        return currency;
    }

    public void setCurrency(WalletCurrency currency) {
        this.currency = currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}
