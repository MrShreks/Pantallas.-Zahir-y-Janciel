package com.example.pantallas.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.InputStream;
import java.util.Properties;
import com.example.pantallas.utils.LoggerUtil;

public class DatabaseConfig {
    private static HikariDataSource dataSource;

    static {
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream("database.properties")) {
            Properties prop = new Properties();
            if (input == null) {
                LoggerUtil.error("No se pudo encontrar database.properties");
            } else {
                prop.load(input);

                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(prop.getProperty("db.url"));
                config.setUsername(prop.getProperty("db.user"));
                config.setPassword(prop.getProperty("db.password"));
                config.setDriverClassName(prop.getProperty("db.driver"));
                
                config.setMaximumPoolSize(Integer.parseInt(prop.getProperty("db.pool.maximumPoolSize", "10")));
                config.setMinimumIdle(Integer.parseInt(prop.getProperty("db.pool.minimumIdle", "2")));
                config.setIdleTimeout(Long.parseLong(prop.getProperty("db.pool.idleTimeout", "30000")));
                config.setConnectionTimeout(Long.parseLong(prop.getProperty("db.pool.connectionTimeout", "30000")));
                config.setMaxLifetime(Long.parseLong(prop.getProperty("db.pool.maxLifetime", "1800000")));
                
                dataSource = new HikariDataSource(config);
            }
        } catch (Exception ex) {
            LoggerUtil.error("Error al inicializar HikariCP: " + ex.getMessage());
        }
    }

    private DatabaseConfig() { }

    public static HikariDataSource getDataSource() {
        return dataSource;
    }
}
