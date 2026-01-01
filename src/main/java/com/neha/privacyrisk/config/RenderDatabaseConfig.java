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

        String cleanJdbcUrl = dbUrl;
        String user = dbUsername;
        String pass = dbPassword;

        try {
            // Check if it's a Render-style URL (postgres:// or postgresql://)
            if (dbUrl != null && (dbUrl.startsWith("postgres://") || dbUrl.startsWith("postgresql://"))) {
                // Parse as a URI
                // Java URI doesn't like "postgres://" scheme sometimes, so we can treat it as
                // http or just use string manipulation
                // But replacing scheme to "http" allows standard URI parsing for
                // host/port/userinfo
                java.net.URI uri = new java.net.URI(
                        dbUrl.replace("postgres://", "http://").replace("postgresql://", "http://"));

                String host = uri.getHost();
                int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                String path = uri.getPath(); // /database

                cleanJdbcUrl = "jdbc:postgresql://" + host + ":" + port + path;

                if (uri.getUserInfo() != null) {
                    String[] userInfo = uri.getUserInfo().split(":");
                    user = userInfo[0];
                    if (userInfo.length > 1) {
                        pass = userInfo[1];
                    }
                }

                System.out.println("DEBUG: Parsed Render URL. Clean JDBC: " + cleanJdbcUrl);
            }
        } catch (Exception e) {
            System.err.println(
                    "Warning: Failed to parse DB_URL: " + e.getMessage() + ". Falling back to simple replacement.");
            if (cleanJdbcUrl.startsWith("postgres://")) {
                cleanJdbcUrl = cleanJdbcUrl.replace("postgres://", "jdbc:postgresql://");
            } else if (cleanJdbcUrl.startsWith("postgresql://")) {
                cleanJdbcUrl = cleanJdbcUrl.replace("postgresql://", "jdbc:postgresql://");
            }
        }

        config.setJdbcUrl(cleanJdbcUrl);
        config.setUsername(user);
        config.setPassword(pass);
        config.setDriverClassName("org.postgresql.Driver");

        return new HikariDataSource(config);
    }
}
