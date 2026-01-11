package org.example.digitalwallet.repository;

import org.example.digitalwallet.model.Wallet;
import org.example.digitalwallet.model.WalletCurrency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Import(WalletRepository.class)
@Sql(scripts = "/test-schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class WalletRepositoryTests {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long testUserId;

    @BeforeEach
    void setUp() {
        // Insert a test user for foreign key constraints
        jdbcTemplate.update(
                "INSERT INTO users (id, email, username, password, subscription_status) VALUES (?, ?, ?, ?, ?)",
                1L, "test@example.com", "testuser", "password", "FREE"
        );
        testUserId = 1L;
    }

    // ========== Create Wallet Tests ==========

    @Test
    void testCreateWallet_Success() {
        // Arrange
        Wallet wallet = Wallet.builder()
                .userId(testUserId)
                .currency(WalletCurrency.EUR)
                .balance(BigDecimal.valueOf(100.00))
                .createdDate(LocalDateTime.now())
                .build();

        // Act
        walletRepository.createWallet(wallet);

        // Assert
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM wallet WHERE user_id = ?",
                Integer.class,
                testUserId
        );
        assertEquals(1, count);
    }

    @Test
    void testCreateWallet_VerifyStoredData() {
        // Arrange
        BigDecimal expectedBalance = BigDecimal.valueOf(250.50);
        LocalDateTime createdDate = LocalDateTime.now();

        Wallet wallet = Wallet.builder()
                .userId(testUserId)
                .currency(WalletCurrency.EUR)
                .balance(expectedBalance)
                .createdDate(createdDate)
                .build();

        // Act
        walletRepository.createWallet(wallet);

        // Assert
        Wallet retrievedWallet = jdbcTemplate.queryForObject(
                "SELECT id, user_id, currency, balance FROM wallet WHERE user_id = ?",
                (rs, rowNum) -> Wallet.builder()
                        .id(rs.getLong("id"))
                        .userId(rs.getLong("user_id"))
                        .currency(WalletCurrency.valueOf(rs.getString("currency")))
                        .balance(rs.getBigDecimal("balance"))
                        .build(),
                testUserId
        );

        assertNotNull(retrievedWallet);
        assertEquals(testUserId, retrievedWallet.getUserId());
        assertEquals(WalletCurrency.EUR, retrievedWallet.getCurrency());
        assertEquals(0, expectedBalance.compareTo(retrievedWallet.getBalance()));
    }

    @Test
    void testCreateWallet_WithZeroBalance() {
        // Arrange
        Wallet wallet = Wallet.builder()
                .userId(testUserId)
                .currency(WalletCurrency.EUR)
                .balance(BigDecimal.ZERO)
                .createdDate(LocalDateTime.now())
                .build();

        // Act
        walletRepository.createWallet(wallet);

        // Assert
        BigDecimal balance = jdbcTemplate.queryForObject(
                "SELECT balance FROM wallet WHERE user_id = ?",
                BigDecimal.class,
                testUserId
        );
        assertEquals(0, BigDecimal.ZERO.compareTo(balance));
    }

    // ========== Get Wallet By User ID Tests ==========

    @Test
    void testGetWalletByUserId_Success() {
        // Arrange
        jdbcTemplate.update(
                "INSERT INTO wallet (user_id, currency, balance, create_time) VALUES (?, ?, ?, ?)",
                testUserId, "EUR", BigDecimal.valueOf(500.00), LocalDateTime.now()
        );

        // Act
        Wallet wallet = walletRepository.getWalletByUserId(testUserId);

        // Assert
        assertNotNull(wallet);
        assertEquals(testUserId, wallet.getUserId());
        assertEquals(WalletCurrency.EUR, wallet.getCurrency());
        assertEquals(0, BigDecimal.valueOf(500.00).compareTo(wallet.getBalance()));
    }

    @Test
    void testGetWalletByUserId_VerifyAllFields() {
        // Arrange
        jdbcTemplate.update(
                "INSERT INTO wallet (user_id, currency, balance, create_time) VALUES (?, ?, ?, ?)",
                testUserId, "EUR", BigDecimal.valueOf(123.45), LocalDateTime.now()
        );

        // Act
        Wallet wallet = walletRepository.getWalletByUserId(testUserId);

        // Assert
        assertNotNull(wallet);
        assertNotNull(wallet.getId());
        assertEquals(testUserId, wallet.getUserId());
        assertEquals(WalletCurrency.EUR, wallet.getCurrency());
        assertEquals(0, BigDecimal.valueOf(123.45).compareTo(wallet.getBalance()));
    }

    @Test
    void testGetWalletByUserId_WithZeroBalance() {
        // Arrange
        jdbcTemplate.update(
                "INSERT INTO wallet (user_id, currency, balance, create_time) VALUES (?, ?, ?, ?)",
                testUserId, "EUR", BigDecimal.ZERO, LocalDateTime.now()
        );

        // Act
        Wallet wallet = walletRepository.getWalletByUserId(testUserId);

        // Assert
        assertNotNull(wallet);
        assertEquals(0, BigDecimal.ZERO.compareTo(wallet.getBalance()));
    }

    // ========== Add Funds Tests ==========

    @Test
    void testAddFunds_Success() {
        // Arrange
        jdbcTemplate.update(
                "INSERT INTO wallet (user_id, currency, balance, create_time) VALUES (?, ?, ?, ?)",
                testUserId, "EUR", BigDecimal.valueOf(100.00), LocalDateTime.now()
        );
        Long walletId = jdbcTemplate.queryForObject("SELECT id FROM wallet WHERE user_id = ?", Long.class, testUserId);

        BigDecimal depositAmount = BigDecimal.valueOf(50.00);

        // Act
        walletRepository.addFunds(depositAmount, walletId);

        // Assert
        BigDecimal newBalance = jdbcTemplate.queryForObject(
                "SELECT balance FROM wallet WHERE id = ?",
                BigDecimal.class,
                walletId
        );
        assertEquals(0, BigDecimal.valueOf(150.00).compareTo(newBalance));
    }

    @Test
    void testAddFunds_MultipleDeposits() {
        // Arrange
        jdbcTemplate.update(
                "INSERT INTO wallet (user_id, currency, balance, create_time) VALUES (?, ?, ?, ?)",
                testUserId, "EUR", BigDecimal.valueOf(100.00), LocalDateTime.now()
        );
        Long walletId = jdbcTemplate.queryForObject("SELECT id FROM wallet WHERE user_id = ?", Long.class, testUserId);

        // Act
        walletRepository.addFunds(BigDecimal.valueOf(25.00), walletId);
        walletRepository.addFunds(BigDecimal.valueOf(75.50), walletId);
        walletRepository.addFunds(BigDecimal.valueOf(10.25), walletId);

        // Assert
        BigDecimal newBalance = jdbcTemplate.queryForObject(
                "SELECT balance FROM wallet WHERE id = ?",
                BigDecimal.class,
                walletId
        );
        assertEquals(0, BigDecimal.valueOf(210.75).compareTo(newBalance));
    }

    @Test
    void testAddFunds_ToZeroBalance() {
        // Arrange
        jdbcTemplate.update(
                "INSERT INTO wallet (user_id, currency, balance, create_time) VALUES (?, ?, ?, ?)",
                testUserId, "EUR", BigDecimal.ZERO, LocalDateTime.now()
        );
        Long walletId = jdbcTemplate.queryForObject("SELECT id FROM wallet WHERE user_id = ?", Long.class, testUserId);

        BigDecimal depositAmount = BigDecimal.valueOf(100.00);

        // Act
        walletRepository.addFunds(depositAmount, walletId);

        // Assert
        BigDecimal newBalance = jdbcTemplate.queryForObject(
                "SELECT balance FROM wallet WHERE id = ?",
                BigDecimal.class,
                walletId
        );
        assertEquals(0, BigDecimal.valueOf(100.00).compareTo(newBalance));
    }

    @Test
    void testAddFunds_LargeAmount() {
        // Arrange
        jdbcTemplate.update(
                "INSERT INTO wallet (user_id, currency, balance, create_time) VALUES (?, ?, ?, ?)",
                testUserId, "EUR", BigDecimal.valueOf(1000.00), LocalDateTime.now()
        );
        Long walletId = jdbcTemplate.queryForObject("SELECT id FROM wallet WHERE user_id = ?", Long.class, testUserId);

        BigDecimal largeDeposit = BigDecimal.valueOf(50000.99);

        // Act
        walletRepository.addFunds(largeDeposit, walletId);

        // Assert
        BigDecimal newBalance = jdbcTemplate.queryForObject(
                "SELECT balance FROM wallet WHERE id = ?",
                BigDecimal.class,
                walletId
        );
        assertEquals(0, BigDecimal.valueOf(51000.99).compareTo(newBalance));
    }

    @Test
    void testAddFunds_PrecisionHandling() {
        // Arrange
        jdbcTemplate.update(
                "INSERT INTO wallet (user_id, currency, balance, create_time) VALUES (?, ?, ?, ?)",
                testUserId, "EUR", new BigDecimal("100.12"), LocalDateTime.now()
        );
        Long walletId = jdbcTemplate.queryForObject("SELECT id FROM wallet WHERE user_id = ?", Long.class, testUserId);

        BigDecimal depositAmount = new BigDecimal("50.88");

        // Act
        walletRepository.addFunds(depositAmount, walletId);

        // Assert
        BigDecimal newBalance = jdbcTemplate.queryForObject(
                "SELECT balance FROM wallet WHERE id = ?",
                BigDecimal.class,
                walletId
        );
        assertEquals(0, new BigDecimal("151.00").compareTo(newBalance));
    }
}
