package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import java.util.Date;

import static com.parkit.parkingsystem.constants.Fare.*;

/**
 * module used to calculate the fare associated to a ticket.
 */
public class FareCalculatorService {

    /**
     * Calculates the fare of the ticket given in parameter.
     * Takes into account the free 30-minute parking
     * @param ticket ticket of the exiting vehicle to be completed with the fare
     */
    public void calculateFare(Ticket ticket) {
        Date outTime = ticket.getOutTime();
        Date inTime = ticket.getInTime();
        if ((outTime == null) || outTime.before(inTime)) {
            throw new IllegalArgumentException(outTime == null
                    ? "Out time provided is null"
                    : "Out time provided is incorrect:" + outTime.toString());
        }

        double inLongTime = inTime.getTime();
        double outLongTime = outTime.getTime();

        double duration = (outLongTime - inLongTime) / MILLISECOND_BY_HOUR;

        //implement Free 30-min parking
        if (duration <= FREE_TIME) {
            duration = 0.0;
        }

        //implement 5%-discount for recurring
        if (ticket.getDiscount()) {
            duration *= (1 - REGULAR_USER_DISCOUNT_RATE);
        }

        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR:
                ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                break;
            case BIKE:
                ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                break;
            default: throw new IllegalArgumentException("Unknown Parking Type");
        }
    }
}
