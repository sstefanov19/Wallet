package org.example.digitalwallet.repository;

import org.example.digitalwallet.model.Transfer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TransferRepository {

    private final JdbcTemplate jdbcTemplate;

    public TransferRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Transfer transfer) {
        String sql = """
                INSERT INTO transfer (from_wallet , to_wallet , currency , transfer_amount, transfer_date)
                VALUES(? , ? , ? , ? , ?)
                """;

        jdbcTemplate.update(sql,
                transfer.getFromWallet(),
                transfer.getToWallet(),
                transfer.getCurrency().name(),
                transfer.getTransferAmount(),
                transfer.getTransferDate());
    }
}
