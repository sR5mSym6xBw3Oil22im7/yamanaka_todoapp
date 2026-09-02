package com.example.todoapp;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class TodoDatabaseMigration {

    private final JdbcTemplate jdbcTemplate;

    public TodoDatabaseMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void addCompletedAtColumnIfNeeded() {
        Integer pinnedColumnCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns "
                        + "WHERE table_schema = DATABASE() AND table_name = 'todos' "
                        + "AND column_name = 'pinned'", Integer.class);
        if (pinnedColumnCount != null && pinnedColumnCount == 0) {
            jdbcTemplate.execute("ALTER TABLE todos ADD COLUMN pinned BOOLEAN NOT NULL DEFAULT FALSE");
        }
        Integer columnCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns "
                        + "WHERE table_schema = DATABASE() "
                        + "AND table_name = 'todos' "
                        + "AND column_name = 'completed_at'",
                Integer.class);

        if (columnCount != null && columnCount == 0) {
            jdbcTemplate.execute("ALTER TABLE todos ADD COLUMN completed_at DATETIME NULL");
        }
        Integer deletedAtColumnCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns "
                        + "WHERE table_schema = DATABASE() AND table_name = 'todos' "
                        + "AND column_name = 'deleted_at'", Integer.class);
        if (deletedAtColumnCount != null && deletedAtColumnCount == 0) {
            jdbcTemplate.execute("ALTER TABLE todos ADD COLUMN deleted_at DATETIME NULL");
        }
    }
}
