package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

/**
 * Class used for retrieving data from table "ticket".
 * ticket table column : ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME
 */
public class TicketDAO {

    private static final Logger LOGGER = LogManager.getLogger("TicketDAO");

    private DataBaseConfig dataBaseConfig;

    /**
     * Constructor of ParkingSpotDAO without parameter.
     */
    public TicketDAO() {
        this.dataBaseConfig = new DataBaseConfig();
    }

    /**
     * Setter of ParkingSpotDAO to associate a new database config.
     * @param dataBaseConfig input parameter
     */
    public void setDataBaseConfig(DataBaseConfig dataBaseConfig) {
        this.dataBaseConfig = dataBaseConfig;
    }

    /**
     * Inserts in the database the data of the ticket of an incoming vehicle.
     * @param ticket The ticket to be inserted in the database
     *
     */
    public void saveTicket(Ticket ticket) {
        try (Connection con = dataBaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(DBConstants.SAVE_TICKET)) {
            ps.setInt(1, ticket.getParkingSpot().getId());
            ps.setString(2, ticket.getVehicleRegNumber());
            ps.setDouble(3, ticket.getPrice());
            ps.setTimestamp(4, new Timestamp(ticket.getInTime().getTime()));
            ps.setTimestamp(5, (ticket.getOutTime() == null)
                    ? null : (new Timestamp(ticket.getOutTime().getTime())));
            ps.setBoolean(6, ticket.getDiscount());
            ps.execute();
        } catch (ClassNotFoundException | SQLException ex) {
            LOGGER.error("Error saving ticket", ex);
        }
    }

    /**
     * Reads the database to retrieve the vehicle ticket parked the parking lot.
     * @param vehicleRegNumber The registration number of the vehicle
     *                        entered in the parking lot and still inside
     * @return the corresponding ticket
     */
    public Ticket getTicket(String vehicleRegNumber) {
        Ticket ticket = null;
        try (Connection con = dataBaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(DBConstants.GET_TICKET)) {
            ps.setString(1, vehicleRegNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ticket = new Ticket();
                    ParkingSpot parkingSpot =
                            new ParkingSpot(rs.getInt(1),
                                    ParkingType.valueOf(rs.getString(7)), false);
                    ticket.setParkingSpot(parkingSpot);
                    ticket.setId(rs.getInt(2));
                    ticket.setVehicleRegNumber(vehicleRegNumber);
                    ticket.setPrice(rs.getDouble(3));
                    ticket.setInTime(rs.getTimestamp(4));
                    ticket.setOutTime(rs.getTimestamp(5));
                    ticket.setDiscount(rs.getBoolean(6));
                }
            }
        } catch (ClassNotFoundException | SQLException ex) {
            LOGGER.error("Error updating saved ticket with discount", ex);
        }

        return ticket;
    }

    /**
     * Checks in the "ticket" table if the user is a recurring one.
     * @param ticket The ticket which need to be check
     * @return True if the user benefits from a 5% discount for recurring
     *  false if not
     */
    public boolean checkRecurringUser(Ticket ticket) {
        boolean recurrent = false;
        try (Connection con = dataBaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(DBConstants.CHECK_RECURRING_USER)) {
            //COUNT(*)
            ps.setString(1, ticket.getVehicleRegNumber());
            try (ResultSet rs = ps.executeQuery()) {
                recurrent = (rs.next() && rs.getInt(1) > 0);
            }
        } catch (ClassNotFoundException | SQLException ex) {
            LOGGER.error("Error while retrieving discount information", ex);
        }
        return recurrent;
    }

    /**
     * Updates the registered ticket in the database.
     * The updated data are the ticket price and the out time
     * @param ticket The ticket to be updated in the database
     * @return true if the database update was completed successfully,
     *  false if not
     */
    public boolean updateTicket(Ticket ticket) {
        boolean updateDone = false;
        try (Connection con = dataBaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(DBConstants.UPDATE_TICKET)) {
            ps.setDouble(1, ticket.getPrice());
            ps.setTimestamp(2, new Timestamp(ticket.getOutTime().getTime()));
            ps.setInt(3, ticket.getId());
            int updateRowCount = ps.executeUpdate();
            updateDone = (updateRowCount == 1);
        } catch (ClassNotFoundException | SQLException ex) {
            LOGGER.error("Error while updating ticket", ex);
        }
        return updateDone;
    }
}
