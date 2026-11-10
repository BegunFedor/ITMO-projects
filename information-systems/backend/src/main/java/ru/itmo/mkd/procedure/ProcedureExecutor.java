package ru.itmo.mkd.procedure;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.itmo.mkd.exception.DatabaseErrorTranslator;

import java.sql.Array;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

@Component
public class ProcedureExecutor {

    private final JdbcTemplate jdbc;
    private final DatabaseErrorTranslator errorTranslator;

    public ProcedureExecutor(JdbcTemplate jdbc, DatabaseErrorTranslator errorTranslator) {
        this.jdbc = jdbc;
        this.errorTranslator = errorTranslator;
    }

    public void call(String sql, Object... args) {
        execute(() -> jdbc.update(sql, args));
    }

    public Long queryForLong(String sql, Object... args) {
        return execute(() -> jdbc.queryForObject(sql, Long.class, args));
    }

    public Integer queryForInt(String sql, Object... args) {
        return execute(() -> jdbc.queryForObject(sql, Integer.class, args));
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(() -> jdbc.query(sql, rowMapper, args));
    }

    public Array bigintArray(Collection<Long> values) {
        Long[] items = values == null ? new Long[0] : values.toArray(Long[]::new);
        return execute(() -> jdbc.execute((ConnectionCallback<Array>) c -> c.createArrayOf("bigint", items)));
    }

    private <T> T execute(Supplier<T> action) {
        try {
            return action.get();
        } catch (DataAccessException e) {
            throw errorTranslator.translate(e);
        }
    }
}
