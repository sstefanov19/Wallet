package org.example.digitalwallet.repository;

import org.example.digitalwallet.model.MembershipStatus;
import org.example.digitalwallet.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Import(UserRepository.class)
@Sql(scripts = "/test-schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ========== Save User Tests ==========

    @Test
    void testSaveUser_Success() {
        // Arrange
        User user = User.builder()
                .email("test@example.com")
                .username("testuser")
                .password("encodedPassword")
                .membershipStatus(MembershipStatus.FREE)
                .build();

        // Act
        userRepository.saveUser(user);

        // Assert
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE username = ?",
                Integer.class,
                "testuser"
        );
        assertEquals(1, count);
    }

    @Test
    void testSaveUser_VerifyStoredData() {
        // Arrange
        User user = User.builder()
                .email("premium@example.com")
                .username("premiumuser")
                .password("hashedPassword123")
                .membershipStatus(MembershipStatus.PREMIUM)
                .build();

        // Act
        userRepository.saveUser(user);

        // Assert
        User savedUser = jdbcTemplate.queryForObject(
                "SELECT * FROM users WHERE username = ?",
                (rs, rowNum) -> User.builder()
                        .id(rs.getLong("id"))
                        .email(rs.getString("email"))
                        .username(rs.getString("username"))
                        .password(rs.getString("password"))
                        .membershipStatus(MembershipStatus.valueOf(rs.getString("subscription_status")))
                        .build(),
                "premiumuser"
        );

        assertNotNull(savedUser);
        assertEquals("premium@example.com", savedUser.getEmail());
        assertEquals("premiumuser", savedUser.getUsername());
        assertEquals("hashedPassword123", savedUser.getPassword());
        assertEquals(MembershipStatus.PREMIUM, savedUser.getMembershipStatus());
    }

    @Test
    void testSaveUser_WithFreeStatus() {
        // Arrange
        User user = User.builder()
                .email("free@example.com")
                .username("freeuser")
                .password("password")
                .membershipStatus(MembershipStatus.FREE)
                .build();

        // Act
        userRepository.saveUser(user);

        // Assert
        String status = jdbcTemplate.queryForObject(
                "SELECT subscription_status FROM users WHERE username = ?",
                String.class,
                "freeuser"
        );
        assertEquals("FREE", status);
    }

    @Test
    void testSaveUser_WithUltraStatus() {
        // Arrange
        User user = User.builder()
                .email("ultra@example.com")
                .username("ultrauser")
                .password("password")
                .membershipStatus(MembershipStatus.ULTRA)
                .build();

        // Act
        userRepository.saveUser(user);

        // Assert
        String status = jdbcTemplate.queryForObject(
                "SELECT subscription_status FROM users WHERE username = ?",
                String.class,
                "ultrauser"
        );
        assertEquals("ULTRA", status);
    }

    @Test
    void testSaveUser_WithNullEmail() {
        // Arrange
        User user = User.builder()
                .email(null)
                .username("noemailuser")
                .password("password")
                .membershipStatus(MembershipStatus.FREE)
                .build();

        // Act
        userRepository.saveUser(user);

        // Assert
        String email = jdbcTemplate.queryForObject(
                "SELECT email FROM users WHERE username = ?",
                String.class,
                "noemailuser"
        );
        assertNull(email);
    }

    // ========== Get User By Username Tests ==========

    @Test
    void testGetUserByUsername_UserExists() {
        // Arrange
        jdbcTemplate.update(
                "INSERT INTO users (email, username, password, subscription_status) VALUES (?, ?, ?, ?)",
                "test@example.com", "existinguser", "encodedPassword", "FREE"
        );

        // Act
        User user = userRepository.getUserByUsername("existinguser");

        // Assert
        assertNotNull(user);
        assertEquals("test@example.com", user.getEmail());
        assertEquals("existinguser", user.getUsername());
        assertEquals("encodedPassword", user.getPassword());
        assertEquals(MembershipStatus.FREE, user.getMembershipStatus());
    }

    @Test
    void testGetUserByUsername_UserDoesNotExist() {
        // Act
        User user = userRepository.getUserByUsername("nonexistentuser");

        // Assert
        assertNull(user);
    }

    @Test
    void testGetUserByUsername_WithPremiumStatus() {
        // Arrange
        jdbcTemplate.update(
                "INSERT INTO users (email, username, password, subscription_status) VALUES (?, ?, ?, ?)",
                "premium@example.com", "premiumuser", "password", "PREMIUM"
        );

        // Act
        User user = userRepository.getUserByUsername("premiumuser");

        // Assert
        assertNotNull(user);
        assertEquals(MembershipStatus.PREMIUM, user.getMembershipStatus());
    }

    @Test
    void testGetUserByUsername_WithUltraStatus() {
        // Arrange
        jdbcTemplate.update(
                "INSERT INTO users (email, username, password, subscription_status) VALUES (?, ?, ?, ?)",
                "ultra@example.com", "ultrauser", "password", "ULTRA"
        );

        // Act
        User user = userRepository.getUserByUsername("ultrauser");

        // Assert
        assertNotNull(user);
        assertEquals(MembershipStatus.ULTRA, user.getMembershipStatus());
    }

    @Test
    void testGetUserByUsername_VerifyAllFields() {
        // Arrange
        jdbcTemplate.update(
                "INSERT INTO users (email, username, password, subscription_status) VALUES (?, ?, ?, ?)",
                "verify@example.com", "verifyuser", "hashedpwd", "FREE"
        );

        // Act
        User user = userRepository.getUserByUsername("verifyuser");

        // Assert
        assertNotNull(user);
        assertNotNull(user.getId());
        assertEquals("verify@example.com", user.getEmail());
        assertEquals("verifyuser", user.getUsername());
        assertEquals("hashedpwd", user.getPassword());
        assertEquals(MembershipStatus.FREE, user.getMembershipStatus());
    }

    @Test
    void testGetUserByUsername_WithNullEmail() {
        // Arrange
        jdbcTemplate.update(
                "INSERT INTO users (email, username, password, subscription_status) VALUES (?, ?, ?, ?)",
                null, "noemailuser", "password", "FREE"
        );

        // Act
        User user = userRepository.getUserByUsername("noemailuser");

        // Assert
        assertNotNull(user);
        assertNull(user.getEmail());
        assertEquals("noemailuser", user.getUsername());
    }

    // ========== Get User ID By Name Tests ==========

    @Test
    void testGetUserIdByName_UserExists() {
        // Arrange
        jdbcTemplate.update(
                "INSERT INTO users (email, username, password, subscription_status) VALUES (?, ?, ?, ?)",
                "test@example.com", "testuser", "password", "FREE"
        );

        // Act
        Long userId = userRepository.getUserIdByName("testuser");

        // Assert
        assertNotNull(userId);
    }

    @Test
    void testGetUserIdByName_UserDoesNotExist() {
        // Act
        Long userId = userRepository.getUserIdByName("nonexistentuser");

        // Assert
        assertNull(userId);
    }

    @Test
    void testGetUserIdByName_MultipleUsersExist() {
        // Arrange
        jdbcTemplate.update(
                "INSERT INTO users (email, username, password, subscription_status) VALUES (?, ?, ?, ?)",
                "user1@example.com", "user1", "password", "FREE"
        );

        jdbcTemplate.update(
                "INSERT INTO users (email, username, password, subscription_status) VALUES (?, ?, ?, ?)",
                "user2@example.com", "user2", "password", "PREMIUM"
        );

        jdbcTemplate.update(
                "INSERT INTO users (email, username, password, subscription_status) VALUES (?, ?, ?, ?)",
                "user3@example.com", "user3", "password", "ULTRA"
        );

        // Act
        Long userId = userRepository.getUserIdByName("user2");

        // Assert
        assertNotNull(userId);
    }
}