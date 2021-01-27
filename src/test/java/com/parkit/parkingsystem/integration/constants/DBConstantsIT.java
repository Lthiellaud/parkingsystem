package com.parkit.parkingsystem.integration.constants;

public class DBConstantsIT {

    public static final String RESET_TICKET_IT = "truncate table ticket";
    public static final String EMPTY_PARKING_IT = "update parking set available = true";
    public static final String FILL_PARKING_IT = "update parking set available = false";
    public static final String OCCUPY_PARKING_IT = "update parking set available = false where PARKING_NUMBER = ?";
    public static final String SAVE_TICKET_IT = "insert " +
            "into ticket(PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME, DISCOUNT) values(1,?,0,?,?,?)";
    //Idem GET_TICKET used for ticketDAO.getTicket() except for the "where" condition.
    //The exit is done => outTime is not null
    public static final String GET_EXITED_VEHICLE_TICKET_IT = "select t.PARKING_NUMBER, t.ID, t.PRICE, t.IN_TIME, "
            + "t.OUT_TIME, t.DISCOUNT, p.TYPE from ticket t,parking p "
            + "where p.parking_number = t.parking_number and t.VEHICLE_REG_NUMBER=?  "
            + "order by t.IN_TIME desc limit 1";

}
