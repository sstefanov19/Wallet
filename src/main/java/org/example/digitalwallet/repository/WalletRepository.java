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

    public Wallet findById(Long id) {
        String sql = "SELECT id, user_id, currency, balance FROM wallet WHERE id = ?";

        return getWallet(id, sql);
    }

    private Wallet getWallet(Long id, String sql) {
        List<Wallet> wallets = jdbcTemplate.query(sql , (rs , rowNum) -> Wallet.builder()
                        .id(rs.getLong("id"))
                        .userId(rs.getLong("user_id"))
                        .currency(WalletCurrency.valueOf(rs.getString("currency")))
                        .balance(rs.getBigDecimal("balance"))
                        .build(),
                        id);

        if (wallets.isEmpty()) {
            return null;
        }

        return wallets.getFirst();
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

        return getWallet(user_id, sql);
    }

    public void addFunds(BigDecimal deposit, Long wallet_id) {

        String sql = """
                UPDATE wallet
                SET balance = COALESCE(balance, 0) + ?
                WHERE id = ?
                """;


        jdbcTemplate.update(sql, deposit , wallet_id);

    }

    public void deductFunds(BigDecimal amount, Long wallet_id) {

        String sql = """
                UPDATE wallet
                SET balance = balance - ?
                WHERE id = ?
                """;

        jdbcTemplate.update(sql, amount, wallet_id);

    }


}
