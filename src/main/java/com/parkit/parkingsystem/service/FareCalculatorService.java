package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException(
                    ticket.getOutTime() == null ?
                            "Out time provided is null" :
                            "Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        double inLongTime = ticket.getInTime().getTime();
        double outLongTime = ticket.getOutTime().getTime();

        double duration = (outLongTime - inLongTime)/(60*60*1000.0);

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                break;
            }
            case BIKE: {
                ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
    }
}