package com.example.urlshortener.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.util.StringUtils;

/**
 * Accepts Neon/Render-style {@code DATABASE_URL} values ({@code postgresql://user:pass@host/db}) in
 * addition to JDBC URLs.
 */
@Configuration
@Profile("postgres")
@EnableConfigurationProperties(DataSourceProperties.class)
public class CloudDataSourceConfig {

    @Bean
    @Primary
    DataSource dataSource(
            DataSourceProperties properties,
            @Value("${DATABASE_URL:}") String databaseUrl,
            @Value("${DATABASE_USERNAME:}") String databaseUsername,
            @Value("${DATABASE_PASSWORD:}") String databasePassword) {
        String jdbcUrl = resolveJdbcUrl(properties.getUrl(), databaseUrl);
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        String user = firstNonBlank(properties.getUsername(), databaseUsername);
        String pass = firstNonBlank(properties.getPassword(), databasePassword);
        if (StringUtils.hasText(user)) {
            config.setUsername(user);
        }
        if (pass != null) {
            config.setPassword(pass);
        }
        if (!jdbcUrl.contains("sslmode=") && jdbcUrl.contains("neon.tech")) {
            config.addDataSourceProperty("sslmode", "require");
        }
        return new HikariDataSource(config);
    }

    static String resolveJdbcUrl(String configuredUrl, String envDatabaseUrl) {
        String candidate = StringUtils.hasText(envDatabaseUrl) ? envDatabaseUrl : configuredUrl;
        if (!StringUtils.hasText(candidate)) {
            throw new IllegalStateException("DATABASE_URL or spring.datasource.url must be set");
        }
        if (candidate.startsWith("jdbc:")) {
            return candidate;
        }
        if (candidate.startsWith("postgres://")) {
            return "jdbc:" + candidate.replaceFirst("^postgres://", "postgresql://");
        }
        if (candidate.startsWith("postgresql://")) {
            return "jdbc:" + candidate;
        }
        return candidate;
    }

    private static String firstNonBlank(String a, String b) {
        if (StringUtils.hasText(a)) {
            return a;
        }
        return StringUtils.hasText(b) ? b : null;
    }
}
