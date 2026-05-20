package com.example.pantallas.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.InputStream;
import java.util.Properties;
import com.example.pantallas.utils.LoggerUtil;

public class DatabaseConfig {
    private static HikariDataSource dataSource;
<<<<<<< HEAD

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
                
=======
    private static boolean initialized = false;
    private static String initError = null;

    static {
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                initError = "No se encontró database.properties en el classpath";
                LoggerUtil.error(initError);
            } else {
                Properties prop = new Properties();
                prop.load(input);

                String url = prop.getProperty("db.url");
                String user = prop.getProperty("db.user");
                String password = prop.getProperty("db.password");

                LoggerUtil.info("Conectando a: " + url + " con usuario: " + user);

                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(url);
                config.setUsername(user);
                config.setPassword(password);
                config.setDriverClassName(prop.getProperty("db.driver"));

>>>>>>> bb658e7 (Primer commit)
                config.setMaximumPoolSize(Integer.parseInt(prop.getProperty("db.pool.maximumPoolSize", "10")));
                config.setMinimumIdle(Integer.parseInt(prop.getProperty("db.pool.minimumIdle", "2")));
                config.setIdleTimeout(Long.parseLong(prop.getProperty("db.pool.idleTimeout", "30000")));
                config.setConnectionTimeout(Long.parseLong(prop.getProperty("db.pool.connectionTimeout", "30000")));
                config.setMaxLifetime(Long.parseLong(prop.getProperty("db.pool.maxLifetime", "1800000")));
<<<<<<< HEAD
                
                dataSource = new HikariDataSource(config);
            }
        } catch (Exception ex) {
            LoggerUtil.error("Error al inicializar HikariCP: " + ex.getMessage());
        }
    }

    private DatabaseConfig() { }
=======

                dataSource = new HikariDataSource(config);
                initialized = true;
                LoggerUtil.info("Conexión a base de datos establecida correctamente");
            }
        } catch (Exception ex) {
            initError = "Error al conectar a la base de datos: " + ex.getMessage();
            LoggerUtil.error(initError);
        }
    }

    private DatabaseConfig() {}
>>>>>>> bb658e7 (Primer commit)

    public static HikariDataSource getDataSource() {
        return dataSource;
    }
<<<<<<< HEAD
=======

    public static boolean isInitialized() {
        return initialized;
    }

    public static String getInitError() {
        return initError;
    }
>>>>>>> bb658e7 (Primer commit)
}
