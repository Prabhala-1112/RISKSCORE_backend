package com.neha.privacyrisk.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class RenderDatabaseConfig {

    @Value("${DB_URL:jdbc:postgresql://localhost:5432/privacyrisk}")
    private String dbUrl;

    @Value("${DB_USERNAME:postgres}")
    private String dbUsername;

    @Value("${DB_PASSWORD:Prabhala@2004}")
    private String dbPassword;

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();

        // Fix Render's postgres:// format to jdbc:postgresql://
        String jdbcUrl = dbUrl;
        if (jdbcUrl.startsWith("postgres://")) {
            jdbcUrl = jdbcUrl.replace("postgres://", "jdbc:postgresql://");
        } else if (jdbcUrl.startsWith("postgresql://")) {
            jdbcUrl = jdbcUrl.replace("postgresql://", "jdbc:postgresql://");
        }

        config.setJdbcUrl(jdbcUrl);
        config.setUsername(dbUsername);
        config.setPassword(dbPassword);
        config.setDriverClassName("org.postgresql.Driver");

        return new HikariDataSource(config);
    }
}
