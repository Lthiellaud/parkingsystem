package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import static com.parkit.parkingsystem.constants.Fare.FREE_TIME;
import static com.parkit.parkingsystem.constants.Fare.REGULAR_USER_DISCOUNT_RATE;

/**
 * module used to calculate the fare associated to a ticket
 */
public class FareCalculatorService {

    /**
     * Calculate the fare of the ticket given in parameter taking into account the free 30-minute parking
     * @param ticket The ticket of the exiting vehicle to be completed with the fare
     */
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

        //implement Free 30-min parking
        if ( duration <= FREE_TIME ) duration = 0.0;

        //implement 5%-discount for recurring
        if (ticket.getDiscount()) duration *= (1 - REGULAR_USER_DISCOUNT_RATE);

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