package com.parkit.parkingsystem.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

import static org.apache.logging.log4j.core.util.Loader.getClassLoader;

/**
 * Data Base Configuration.
 */
public class DataBaseConfig {

    private static final Logger LOGGER = LogManager.getLogger("DataBaseConfig");

    /**
     * Method to create a connection on the MySQL database of th application.
     * @return A data base connection
     * @throws ClassNotFoundException
     * @throws SQLException
     */
     public Connection getConnection() throws ClassNotFoundException, SQLException {

         LOGGER.info("Create DB connection");

         Properties prop = new Properties();
         String dbUrl = "";
         String user = "";
         String password = "";
         String driverClass = "";

         try (InputStream in = getClassLoader().getResourceAsStream("database.properties")) {
             prop.load(in);
             dbUrl = prop.getProperty("dbUrl");
             user = prop.getProperty("user");
             password = prop.getProperty("password");
             driverClass = prop.getProperty("driverClass");
         } catch (FileNotFoundException e) {
             LOGGER.error("database.properties file is missing", e);
         } catch (IOException e) {
             LOGGER.error("Error while reading database.properties file", e);
         }

         Class.forName(driverClass);
         return DriverManager.getConnection(dbUrl, user, password);
    }

    /**
     * Closes the given database connection.
     * @param con database connection ti be closed
     */
    public void closeConnection(Connection con) {
        if (con != null) {
            try {
                con.close();
                LOGGER.info("Closing DB connection");
            } catch (SQLException e) {
                LOGGER.error("Error while closing connection", e);
            }
        }
    }

    /**
     * Closes a prepared statement.
     * @param ps the prepared statement to be closed
     */
    public void closePreparedStatement(PreparedStatement ps) {
        if (ps != null) {
            try {
                ps.close();
                LOGGER.info("Closing Prepared Statement");
            } catch (SQLException e) {
                LOGGER.error("Error while closing prepared statement", e);
            }
        }
    }

    /**
     * Closes a result set.
     * @param rs The result set to be closed
     */
    public void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
                LOGGER.info("Closing Result Set");
            } catch (SQLException e) {
                LOGGER.error("Error while closing result set", e);
            }
        }
    }
}
