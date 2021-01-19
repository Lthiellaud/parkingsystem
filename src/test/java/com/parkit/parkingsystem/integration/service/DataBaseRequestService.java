package com.parkit.parkingsystem.integration.service;

import com.mysql.cj.jdbc.CallableStatement;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.constants.DBConstantsIT;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;

import static com.parkit.parkingsystem.constants.Fare.FREE_TIME;

public class DataBaseRequestService {

    DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();

    public Ticket getLastTicket(String vehicleRegNumber){
        Connection con = null;
        Ticket ticket = null;
        try {
            con = dataBaseTestConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstantsIT.GET_LAST_TICKET_IT);
            //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
            ps.setString(1,vehicleRegNumber);
            System.out.println(ps.toString());
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                ticket = new Ticket();
                ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(1), ParkingType.valueOf(rs.getString(6)),false);
                ticket.setParkingSpot(parkingSpot);
                ticket.setId(rs.getInt(2));
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(rs.getDouble(3));
                ticket.setInTime(rs.getTimestamp(4));
                ticket.setOutTime(rs.getTimestamp(5));
            }
            dataBaseTestConfig.closeResultSet(rs);
            dataBaseTestConfig.closePreparedStatement(ps);

        }catch (Exception ex){
            ex.printStackTrace();
        }finally {
            dataBaseTestConfig.closeConnection(con);
        }
        return ticket;
    }

    public boolean createTickets (boolean newIncoming, String vehicleRegNumber){
        Connection con = null;
        boolean saveDone = false;
        try {
            con = dataBaseTestConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstantsIT.SAVE_TICKET_IT);

            ps.setString(1, vehicleRegNumber);
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
            saveDone =  ps.execute();
        }catch (Exception ex){
            ex.printStackTrace();
        }finally {
            dataBaseTestConfig.closeConnection(con);
        }
        return saveDone;
    }


}
