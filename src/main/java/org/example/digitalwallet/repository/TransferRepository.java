package org.example.digitalwallet.repository;

import org.example.digitalwallet.model.Transfer;
import org.example.digitalwallet.model.WalletCurrency;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    public List<Transfer> findTransfers(Long cursor, int limit) {
        String sql;
        if(cursor == null) {
            //First page
            sql = """
                    SELECT * FROM transfer
                    ORDER BY id DESC
                    LIMIT ?
                    """;

            return jdbcTemplate.query(sql ,transferRowMapper ,limit);
        }else {
            // Next page
            sql = """
                SELECT * FROM transfer
                WHERE id < ?
                ORDER BY id DESC
                LIMIT ?
            """;
            return jdbcTemplate.query(sql, transferRowMapper, cursor, limit);
        }
    }


    private final RowMapper<Transfer> transferRowMapper = (rs, rowNum) -> Transfer.builder()
            .id(rs.getLong("id"))
            .fromWallet(rs.getLong("from_wallet"))
            .toWallet(rs.getLong("to_wallet"))
            .currency(WalletCurrency.valueOf(rs.getString("currency")))
            .transferAmount(rs.getBigDecimal("transfer_amount"))
            .transferDate(rs.getTimestamp("transfer_date").toLocalDateTime())
            .build();

}
