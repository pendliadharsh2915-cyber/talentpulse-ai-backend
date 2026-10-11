package com.jobportal.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
@Profile("prod")
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${DATABASE_URL:#{null}}")
    private String databaseUrlEnv;

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        String url = properties.getUrl();
        String username = properties.getUsername();
        String password = properties.getPassword();

        // If standard properties.getUrl() is missing or default localhost, but DATABASE_URL exists
        if ((url == null || url.contains("localhost:5432/jobportal")) && databaseUrlEnv != null && !databaseUrlEnv.isBlank()) {
            url = databaseUrlEnv;
        }

        // Support cloud PostgreSQL URL formats (postgres:// or postgresql:// without jdbc: prefix)
        if (url != null && (url.startsWith("postgres://") || (url.startsWith("postgresql://") && !url.startsWith("jdbc:")))) {
            try {
                URI uri = new URI(url.replace("postgres://", "postgresql://"));
                String userInfo = uri.getUserInfo();
                if (userInfo != null && userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    username = java.net.URLDecoder.decode(parts[0], java.nio.charset.StandardCharsets.UTF_8);
                    password = java.net.URLDecoder.decode(parts[1], java.nio.charset.StandardCharsets.UTF_8);
                }
                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                url = "jdbc:postgresql://" + uri.getHost() + ":" + port + uri.getPath();
                if (uri.getQuery() != null) {
                    url += "?" + uri.getQuery();
                }
                log.info("Configured cloud PostgreSQL data source for host: {}, port: {}, database: {}", uri.getHost(), port, uri.getPath());
            } catch (Exception e) {
                log.warn("Failed to parse cloud database URI, falling back to raw URL: {}", e.getMessage());
            }
        }

        return properties.initializeDataSourceBuilder()
                .url(url)
                .username(username)
                .password(password)
                .build();
    }
}
