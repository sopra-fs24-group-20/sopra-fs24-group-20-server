package ch.uzh.ifi.hase.soprafs24.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DatabaseCleanupService implements ApplicationListener<ContextClosedEvent> {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DatabaseCleanupService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        try {
            dropTables();
        } catch (RuntimeException e) {
            System.err.println("Critical error during shutdown cleanup: " + e.getMessage());
            throw e; // Re-throw to make this error visible outside if necessary.
        }
    }

    private void dropTables() {
        String[] tablesToDrop = {"GAME", "LOBBY"};
        for (String table : tablesToDrop) {
            try {
                jdbcTemplate.execute("DROP TABLE IF EXISTS " + table);
                System.out.println("Dropped table: " + table);
            } catch (Exception e) {
                throw new RuntimeException("Failed to drop table " + table, e);
            }
        }
    }
}

