package com.parkit.parkingsystem.integration.config;

import com.parkit.parkingsystem.config.DataBaseConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Resources;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

import static org.apache.logging.log4j.core.util.Loader.getClassLoader;

public class DataBaseTestConfig extends DataBaseConfig {

    private static final Logger LOGGER = LogManager.getLogger("DataBaseTestConfig");

    @Override
    public Connection getConnection() throws ClassNotFoundException, SQLException {
        LOGGER.info("Create DB connection");

        Properties prop = new Properties();
        String dbUrl = "";
        String user = "";
        String password = "";
        String driverClass = "";

        try (InputStream in = getClassLoader().getResourceAsStream("database_Test.properties")) {
            prop.load(in);
            dbUrl = prop.getProperty("dbUrl");
            user = prop.getProperty("user");
            password = prop.getProperty("password");
            driverClass = prop.getProperty("driverClass");
        } catch (FileNotFoundException e) {
            LOGGER.error("database_Test.properties file is missing", e);
        } catch (IOException e) {
            LOGGER.error("Error while reading database_Test.properties file", e);
        }
        Class.forName(driverClass);
        return DriverManager.getConnection(dbUrl, user, password);    }

}
