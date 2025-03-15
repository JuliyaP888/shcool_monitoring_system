package com.prishedko.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.util.Properties;

public class DatabaseConfig {
    private static final HikariDataSource dataSource;

    static {
        Properties props = new Properties();
        try {
            props.load(DatabaseConfig.class.getClassLoader().getResourceAsStream("db.properties"));
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.username"));
            config.setPassword(props.getProperty("db.password"));
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.poolSize")));
            dataSource = new HikariDataSource(config);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load database configuration", e);
        }
    }

    public static HikariDataSource getDataSource() {
        return dataSource;
    }
}