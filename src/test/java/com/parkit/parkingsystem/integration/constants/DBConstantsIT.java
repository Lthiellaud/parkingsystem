package com.parkit.parkingsystem.integration.constants;

public class DBConstantsIT {

    public static final String RESET_TICKET_IT = "truncate table ticket";
    public static final String RESET_PARKING_IT = "update parking set available = true";
    public static final String SAVE_TICKET_IT = "insert into ticket(PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME) values(1,?,0,?,?)";
    public static final String GET_LAST_TICKET_IT = "select t.PARKING_NUMBER, t.ID, t.PRICE, t.IN_TIME, t.OUT_TIME, p.TYPE "
            + "from ticket t,parking p "
            + "where p.parking_number = t.parking_number and t.VEHICLE_REG_NUMBER=?  "
            + "order by t.IN_TIME desc limit 1";

}
