package org.example.digitalwallet.repository;

import org.example.digitalwallet.model.Wallet;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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


}
