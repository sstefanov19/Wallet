package org.example.digitalwallet.repository;

import org.example.digitalwallet.model.MembershipStatus;
import org.example.digitalwallet.model.User;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepository {

    private JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        List<User> users = jdbcTemplate.query(sql ,  (rs, rowNum) -> User.builder()
                .email(rs.getString("email"))
                .username(rs.getString("username"))
                .password(rs.getString("password"))
                .membershipStatus(MembershipStatus.valueOf(rs.getString("subscription_status")))
                .build(),
                username
        );

        return users.isEmpty() ? null : users.getFirst();
    }

    public Long getUserIdByName(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";

        try {
            return jdbcTemplate.queryForObject(sql , Long.class , username);
        }catch(EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void saveUser(User user) {
        String sql = """
                INSERT INTO users (email , username, password , subscription_status)
                VALUES (? , ? , ?, ?)
                """;

        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getUsername(),
                user.getPassword(),
                user.getMembershipStatus().name()
        );
    }

}
