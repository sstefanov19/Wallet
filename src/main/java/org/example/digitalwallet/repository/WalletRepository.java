package org.example.digitalwallet.repository;

import org.example.digitalwallet.model.Wallet;
import org.example.digitalwallet.model.WalletCurrency;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class WalletRepository {

    private static final String WALLET_CACHE = "wallets";
    private static final String WALLET_BY_USER_CACHE = "walletsByUser";


    private final JdbcTemplate jdbcTemplate;

    public WalletRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Cacheable(value = WALLET_CACHE, key = "#id", unless = "#result == null")
    public Wallet findById(Long id) {
        String sql = "SELECT id, user_id, currency, balance FROM wallet WHERE id = ?";

        return getWallet(id, sql);
    }

    public Wallet findByIdForUpdate(Long id) {
        String sql = "SELECT id, user_id, currency, balance FROM wallet WHERE id = ? FOR UPDATE";

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


    @CacheEvict(value = WALLET_BY_USER_CACHE, key = "#wallet.userId")
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

    @Cacheable(value = WALLET_BY_USER_CACHE, key = "#user_id", unless = "#result == null")
    public Wallet getWalletByUserId(Long user_id) {
        String sql = "SELECT id , user_id, currency , balance FROM wallet where user_id = ?";

        return getWallet(user_id, sql);
    }

    @CacheEvict(value = WALLET_CACHE, key = "#wallet_id")
    public void addFunds(BigDecimal deposit, Long wallet_id) {

        String sql = """
                UPDATE wallet
                SET balance = COALESCE(balance, 0) + ?
                WHERE id = ?
                """;


        jdbcTemplate.update(sql, deposit , wallet_id);

    }

    @CacheEvict(value = WALLET_CACHE, key = "#wallet_id")
    public boolean deductFunds(BigDecimal amount, Long wallet_id) {

        String sql = """
                UPDATE wallet
                SET balance = balance - ?
                WHERE id = ? AND balance >= ?
                """;

        int rowsEffected = jdbcTemplate.update(sql, amount, wallet_id, amount);
        return rowsEffected > 0;

    }

    @CacheEvict(value = WALLET_CACHE, allEntries = true)
    public boolean executeTransfer(Long fromWalletId, Long toWalletId, BigDecimal amount) {
        // Lock in consistent order to prevent deadlocks
        Long firstId = Math.min(fromWalletId, toWalletId);
        Long secondId = Math.max(fromWalletId, toWalletId);

        String sql = """
                WITH locked AS (
                    SELECT id FROM wallet WHERE id IN (?, ?) ORDER BY id FOR UPDATE
                ),
                deduct AS (
                    UPDATE wallet SET balance = balance - ?
                    WHERE id = ? AND balance >= ?
                    RETURNING id
                ),
                credit AS (
                    UPDATE wallet SET balance = balance + ?
                    WHERE id = ? AND EXISTS (SELECT 1 FROM deduct)
                    RETURNING id
                )
                SELECT EXISTS (SELECT 1 FROM credit) as success
                """;

        Boolean success = jdbcTemplate.queryForObject(sql, Boolean.class,
                firstId, secondId,
                amount, fromWalletId, amount,
                amount, toWalletId);

        return Boolean.TRUE.equals(success);
    }

}
