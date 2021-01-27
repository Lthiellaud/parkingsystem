package com.parkit.parkingsystem.integration.service;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.constants.DBConstantsIT;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import static com.parkit.parkingsystem.constants.Fare.FREE_TIME;
import static com.parkit.parkingsystem.integration.constants.DBConstantsIT.FILL_PARKING_IT;
import static com.parkit.parkingsystem.integration.constants.DBConstantsIT.OCCUPY_PARKING_IT;

public class DataBaseRequestService {

    DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();

    /**
     * Idem ticketDAO.getTicket() except for the "where" condition.
     * The exit is done : outTime is not null
     * @param vehicleRegNumber registration number of the vehicle that has just exited.
     * @return the registered ticket
     */
    public Ticket getExitedVehicleTicket(String vehicleRegNumber){

        Ticket ticket = null;
        try (Connection con = dataBaseTestConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(DBConstantsIT.GET_EXITED_VEHICLE_TICKET_IT)) {

            //PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME, DISCOUNT, TYPE)
            ps.setString(1,vehicleRegNumber);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                ticket = new Ticket();
                ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(1), ParkingType.valueOf(rs.getString(7)),false);
                ticket.setParkingSpot(parkingSpot);
                ticket.setId(rs.getInt(2));
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(rs.getDouble(3));
                ticket.setInTime(rs.getTimestamp(4));
                ticket.setOutTime(rs.getTimestamp(5));
                ticket.setDiscount(rs.getBoolean(6));
            }

        } catch (Exception ex){
            ex.printStackTrace();
        }
        return ticket;
    }

    /**
     * Insert lines in ticket table.
     * to simulate ancient ticket and new incoming sometime ago.
     * @param newIncoming true to create a ticket with inTime = now - 35mn, outTime = null
     *                    false to create a ticket with outTime not null
     * @param vehicleRegNumber The registration number of the parked vehicle
     * @param discount true if the user is a recurring one
     *
     */
    public void createTickets (boolean newIncoming, String vehicleRegNumber, boolean discount){

        try (Connection con = dataBaseTestConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(DBConstantsIT.SAVE_TICKET_IT)) {

            ps.setString(1, vehicleRegNumber);
            ps.setBoolean(4, discount);
            if (newIncoming) {
                //Entrance 35mn ago (free time + 5mn)
                int duration = (int) (FREE_TIME*60*60*1000+300000) ;
                ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()-duration));
                ps.setTimestamp(3, null);
            } else {
                //one day old ticket / duration 25mn (free time - 5mn)
                int duration = (int) (FREE_TIME*60*60*1000-300000) ;
                ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()-24*60*60*1000-duration));
                ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()-24*60*60*1000));
            }
            ps.execute();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    /**
     * Set AVAILABLE to false for all lines in parking to simulate that the parking is full.
     */
    public void fillParking(){
        try (Connection connection = dataBaseTestConfig.getConnection();
            //set parking entries to occupied
             PreparedStatement ps = connection.prepareStatement(FILL_PARKING_IT)) {

            ps.execute();

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Set AVAILABLE to false where PARKING_NUMBER=parkingNumber in parking.
     *  to simulate that the parking number parkingNumber is occupied.
     * @param parkingNumber parkingNumber that has to be occupied
     */
    public void occupyParking(int parkingNumber) {
         try (Connection connection = dataBaseTestConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(OCCUPY_PARKING_IT)) {

            ps.setInt(1, parkingNumber);
            //set parking "parkingNumber" to occupied
            ps.execute();

        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
