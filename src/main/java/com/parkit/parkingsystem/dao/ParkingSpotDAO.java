package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Class used for retrieving data from table "parking".
 */
public class ParkingSpotDAO {

    private static final Logger LOGGER = LogManager.getLogger("ParkingSpotDAO");
    private DataBaseConfig dataBaseConfig;

    /**
     * Constructor of ParkingSpotDAO with database config as a parameter.
     * @param dataBaseConfig input parameter
     */
    public ParkingSpotDAO(final DataBaseConfig dataBaseConfig) {
        this.dataBaseConfig = dataBaseConfig;
    }

    /**
     * Constructor of ParkingSpotDAO without parameter.
     */
    public ParkingSpotDAO() {
        this.dataBaseConfig = new DataBaseConfig();
    }
    /**
     * Sends the number of the next available slot found in the parking table
     * for the given vehicle type.
     * @param parkingType Parking type which is needed
     * @return the number of the next available slot for the given vehicle
     *  type if it exists, (-1) if not.
     */
     public int getNextAvailableSlot(ParkingType parkingType) {
        int result = -1;
        try (Connection con = dataBaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(DBConstants.GET_NEXT_PARKING_SPOT)) {
            ps.setString(1, parkingType.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    result = rs.getInt(1);
                }
            }
        } catch (ClassNotFoundException | SQLException ex) {
            LOGGER.error("Error fetching next available slot in the database", ex);
        }
        return result;
    }

    /**
     * Updates available in "parking" table for the given ParkingSpot instance.
     * @param parkingSpot the parking spot to be updated
     * @return true if the updte is Ok, false if not
     */
    public boolean updateParking(ParkingSpot parkingSpot) {
        try (Connection con = dataBaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(DBConstants.UPDATE_PARKING_SPOT)) {
            ps.setBoolean(1, parkingSpot.isAvailable());
            ps.setInt(2, parkingSpot.getId());
            int updateRowCount = ps.executeUpdate();
            return (updateRowCount == 1);
        } catch (ClassNotFoundException | SQLException ex) {
            LOGGER.error("Error updating parking info", ex);
            return false;
        }
    }

}
