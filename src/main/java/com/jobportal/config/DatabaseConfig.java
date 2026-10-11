package com.jobportal.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Configuration
@Profile("prod")
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${SPRING_DATASOURCE_URL:#{null}}")
    private String springDatasourceUrl;

    @Value("${DATABASE_URL:#{null}}")
    private String databaseUrl;

    @Value("${SPRING_DATASOURCE_USERNAME:#{null}}")
    private String defaultUsername;

    @Value("${SPRING_DATASOURCE_PASSWORD:#{null}}")
    private String defaultPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        String url = springDatasourceUrl;
        if (url == null || url.isBlank()) {
            url = databaseUrl;
        }

        String username = defaultUsername;
        String password = defaultPassword;

        if (url != null && (url.startsWith("postgres://") || url.startsWith("postgresql://"))) {
            if (!url.startsWith("jdbc:")) {
                try {
                    URI uri = new URI(url.replace("postgres://", "postgresql://"));
                    String userInfo = uri.getUserInfo();
                    if (userInfo != null && userInfo.contains(":")) {
                        String[] parts = userInfo.split(":", 2);
                        username = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                        password = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                    }
                    int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                    url = "jdbc:postgresql://" + uri.getHost() + ":" + port + uri.getPath();
                    if (uri.getQuery() != null) {
                        url += "?" + uri.getQuery();
                    }
                    log.info("Parsed cloud PostgreSQL URI for host: {}, port: {}, path: {}", uri.getHost(), port, uri.getPath());
                } catch (Exception e) {
                    log.warn("Failed to parse URI: {}, using raw URL", e.getMessage());
                }
            }
        }

        if (url == null || url.isBlank()) {
            url = "jdbc:postgresql://localhost:5432/jobportal";
        }
        if (username == null) {
            username = "talentpulse_user";
        }
        if (password == null) {
            password = "postgres";
        }

        log.info("Initializing HikariDataSource with JDBC URL: {}", url);

        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setJdbcUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setMaximumPoolSize(10);
        ds.setMinimumIdle(2);
        ds.setConnectionTimeout(30000);
        ds.setIdleTimeout(600000);
        ds.setMaxLifetime(1800000);
        return ds;
    }
}
