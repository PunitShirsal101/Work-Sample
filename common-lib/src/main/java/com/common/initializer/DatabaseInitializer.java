package com.common.initializer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static com.common.logging.DatabaseLogMessages.*;

@Component
public class DatabaseInitializer implements CommandLineRunner {
    private static final Log log = LogFactory.getLog(DatabaseInitializer.class);

    @Value("${spring.datasource.url}")
    private String adminUrl;

    @Value("${spring.datasource.username}")
    private String adminUsername;

    @Value("${spring.datasource.password}")
    private String adminPassword;

    @Value("${app.database.name}")
    private String targetDatabase;

    @Override
    public void run(String... args) {
        try (Connection connection = DriverManager.getConnection(adminUrl, adminUsername, adminPassword);
             Statement statement = connection.createStatement()) {

            String checkDbExistsQuery = "SELECT 1 FROM pg_database WHERE datname = '" + targetDatabase + "'";
            var resultSet = statement.executeQuery(checkDbExistsQuery);

            if (!resultSet.next()) {
                String createDbQuery = "CREATE DATABASE " + targetDatabase;
                statement.executeUpdate(createDbQuery);
                log.info(MSG_DB_PREFIX + targetDatabase + MSG_DB_CREATED_SUFFIX);
            } else {
                log.info(MSG_DB_PREFIX + targetDatabase + MSG_DB_EXISTS_SUFFIX);
            }

        } catch (SQLException e) {
            log.error(MSG_ERROR_CREATING_DB + e.getMessage(), e);
        }
    }
}
