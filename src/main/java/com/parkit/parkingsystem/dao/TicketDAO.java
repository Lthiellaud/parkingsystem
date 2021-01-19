package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

/**
 * Class used for retrieving data from table "ticket".
 */
public class TicketDAO {

    private static final Logger LOGGER = LogManager.getLogger("TicketDAO");

    public DataBaseConfig dataBaseConfig = new DataBaseConfig();

    /**
     * Inserts in the database the data of the ticket of an incoming vehicle.
     * @param ticket Th ticket to be inserted in the database
     * @return true if the database insert was completed successfully,
     *  false if not
     */
    public boolean saveTicket(Ticket ticket) {
        Connection con = null;
        boolean saveDone = false;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps
                    = con.prepareStatement(DBConstants.SAVE_TICKET);
            ps.setInt(1, ticket.getParkingSpot().getId());
            ps.setString(2, ticket.getVehicleRegNumber());
            ps.setDouble(3, ticket.getPrice());
            ps.setTimestamp(4, new Timestamp(ticket.getInTime().getTime()));
            ps.setTimestamp(5, (ticket.getOutTime() == null)
                    ? null : (new Timestamp(ticket.getOutTime().getTime())));
            saveDone =  ps.execute();
        } catch (Exception ex) {
            LOGGER.error("Error saving ticket", ex);
        } finally {
            dataBaseConfig.closeConnection(con);
        }
        return saveDone;
    }

    /**
     * Reads the database to retrieve the vehicle ticket parked the parking lot.
     * @param vehicleRegNumber The registration number of the vehicle
     *                        entered in the parking lot and still inside
     * @return the corresponding ticket
     */
    public Ticket getTicket(String vehicleRegNumber) {
        Connection con = null;
        Ticket ticket = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.GET_TICKET);
            //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
            ps.setString(1, vehicleRegNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ticket = new Ticket();
                ParkingSpot parkingSpot =
                    new ParkingSpot(rs.getInt(1),
                        ParkingType.valueOf(rs.getString(6)), false);
                ticket.setParkingSpot(parkingSpot);
                ticket.setId(rs.getInt(2));
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(rs.getDouble(3));
                ticket.setInTime(rs.getTimestamp(4));
                ticket.setOutTime(rs.getTimestamp(5));
            }
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);

        } catch (Exception ex) {
            LOGGER.error("Error updating saved ticket with discount", ex);
        } finally {
            dataBaseConfig.closeConnection(con);
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
        Connection con = null;
        boolean recurrent = false;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps
                    = con.prepareStatement(DBConstants.CHECK_RECURRING_USER);
            //COUNT(*)
            ps.setString(1, ticket.getVehicleRegNumber());
            ResultSet rs = ps.executeQuery();
            recurrent = (rs.next() && rs.getInt(1) > 1);
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
        } catch (Exception ex) {
            LOGGER.error("Error while retrieving discount information", ex);
        } finally {
            dataBaseConfig.closeConnection(con);
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
        Connection con = null;
        boolean updateDone = false;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps
                    = con.prepareStatement(DBConstants.UPDATE_TICKET);
            ps.setDouble(1, ticket.getPrice());
            ps.setTimestamp(2, new Timestamp(ticket.getOutTime().getTime()));
            ps.setInt(3, ticket.getId());
            int updateRowCount = ps.executeUpdate();
            updateDone = (updateRowCount == 1);
            dataBaseConfig.closePreparedStatement(ps);
        } catch (Exception ex) {
            LOGGER.error("Error updating ticket info", ex);
        } finally {
            dataBaseConfig.closeConnection(con);
        }
        return updateDone;
    }
}
