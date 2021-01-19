package com.parkit.parkingsystem.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

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
    @SuppressWarnings("checkstyle:NoWhitespaceBefore")
    public Connection getConnection() throws ClassNotFoundException,
                                             SQLException {
        LOGGER.info("Create DB connection");
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/prod"
          + "?useUnicode=true"
          + "&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&"
          + "serverTimezone=UTC",
          "root", "rootroot");
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
