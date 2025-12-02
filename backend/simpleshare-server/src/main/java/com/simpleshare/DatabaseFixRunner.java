package com.simpleshare;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseFixRunner implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            // Check if database fix is needed
            try {
                jdbcTemplate.queryForObject("SELECT enable_tiered_read FROM article LIMIT 1", String.class);
                System.out.println("Database schema is up to date");
            } catch (Exception e) {
                System.out.println("Database fix needed. Applying fixes...");
                fixDatabaseSchema();
                System.out.println("Database fix completed");
            }
        } catch (Exception e) {
            System.err.println("Failed to check/fix database: " + e.getMessage());
        }
    }

    private void fixDatabaseSchema() {
        String[] fixStatements = {
            "ALTER TABLE article ADD COLUMN enable_tiered_read TINYINT NOT NULL DEFAULT 1 AFTER status",
            "ALTER TABLE article ADD COLUMN allow_copy TINYINT NULL DEFAULT 1 AFTER enable_tiered_read",
            "ALTER TABLE article ADD COLUMN preview_content LONGTEXT NULL AFTER allow_copy",
            "ALTER TABLE article ADD COLUMN enable_watermark TINYINT NULL DEFAULT 1 AFTER seo_description"
        };

        for (String sql : fixStatements) {
            try {
                jdbcTemplate.execute(sql);
                System.out.println("Executed: " + sql);
            } catch (Exception e) {
                if (e.getMessage().contains("Duplicate column name") || e.getMessage().contains("1060")) {
                    System.out.println("Column already exists: " + sql);
                } else {
                    System.err.println("Failed to execute: " + sql + " - " + e.getMessage());
                }
            }
        }
    }
}