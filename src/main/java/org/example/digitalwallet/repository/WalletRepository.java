package org.example.digitalwallet.repository;

import org.example.digitalwallet.model.Wallet;
import org.example.digitalwallet.model.WalletCurrency;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class WalletRepository {


    private final JdbcTemplate jdbcTemplate;

    public WalletRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public void createWallet(Wallet wallet) {
        String sql = """
            INSERT INTO wallet (user_id, currency,balance ,create_time)
            VALUES (?, ?,?,?)
            """;

        jdbcTemplate.update(sql,
                wallet.getUserId(),
                wallet.getCurrency().name(),
                wallet.getBalance(),
                wallet.getCreatedDate());
    }

    public Wallet getWalletByUserId(Long user_id) {
        String sql = "SELECT id , user_id, currency , balance FROM wallet where user_id = ?";

        List<Wallet> wallets =  jdbcTemplate.query(sql , (rs, rowNum) -> Wallet.builder()
                .id(rs.getLong("id"))
                .userId(rs.getLong("user_id"))
                .currency(WalletCurrency.valueOf(rs.getString("currency")))
                .balance(rs.getBigDecimal("balance"))
                .build(),
                user_id);

        return wallets.getFirst();
    }

    public void addFunds(BigDecimal deposit, Long wallet_id) {

        String sql = """
                UPDATE wallet
                SET balance = COALESCE(balance, 0) + ?
                WHERE id = ?
                """;


        jdbcTemplate.update(sql, deposit , wallet_id);

    }


}
