package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

public class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;

    @BeforeAll
    private static void setUp() {
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    private void setUpPerTest() {
        ticket = new Ticket();
    }

    @Test
    @DisplayName("1h Calculated fare for a car = Fare.CAR_RATE_PER_HOUR")
    public void calculateFareCar(){
        //GIVEN
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - Fare.MILLISECOND_BY_HOUR );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        ticket.setDiscount(false);

        //WHEN
        fareCalculatorService.calculateFare(ticket);

        //THEN
        assertEquals(Fare.CAR_RATE_PER_HOUR, ticket.getPrice());
    }

    @Test
    @DisplayName("1h Calculated fare for a bike = Fare.BIKE_RATE_PER_HOUR")
    public void calculateFareBike(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - Fare.MILLISECOND_BY_HOUR );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        ticket.setDiscount(false);

        fareCalculatorService.calculateFare(ticket);

        assertEquals(Fare.BIKE_RATE_PER_HOUR, ticket.getPrice());
    }


    @Test
    @DisplayName("Calculate fare with future inTime throws an exception")
    public void calculateFareBikeWithFutureInTime(){
        //GIVEN
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() + Fare.MILLISECOND_BY_HOUR );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        ticket.setDiscount(false);

        //WHEN
        Exception exception = assertThrows(IllegalArgumentException.class, ()
                -> fareCalculatorService.calculateFare(ticket));

        //THEN
        assertThat(exception.getMessage()).contains("Out time provided is incorrect:");
    }

    @Test
    @DisplayName("Calculate fare with null out time throws an exception")
    public void calculateFareBikeWithNullOutTime(){
        //GIVEN
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - Fare.MILLISECOND_BY_HOUR );
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(null);
        ticket.setParkingSpot(parkingSpot);
        ticket.setDiscount(false);

        //WHEN
        Exception exception = assertThrows(IllegalArgumentException.class, ()
                -> fareCalculatorService.calculateFare(ticket));

        //THEN
        assertThat(exception.getMessage()).contains("Out time provided is null");
    }

    @Test
    @DisplayName("Calculate 45 mn fare for a car should be 0.75*car fare rate")
    public void calculateFareBikeWithLessThanOneHourParkingTime(){
        //GIVEN
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (long) (Fare.MILLISECOND_BY_HOUR * 0.75) );//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        ticket.setDiscount(false);

        //WHEN
        fareCalculatorService.calculateFare(ticket);

        //THEN
        assertEquals((0.75 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());
    }

    @Test
    @DisplayName("Calculate 45 mn fare for a bike should be 0.75*bike fare rate")
    public void calculateFareCarWithLessThanOneHourParkingTime(){
        //GIVEN
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (long) (Fare.MILLISECOND_BY_HOUR * 0.75) );//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        ticket.setDiscount(false);

        //WHEN
        fareCalculatorService.calculateFare(ticket);

        //THEN
        assertEquals( (0.75 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
    }

    @Test
    @DisplayName("Calculate 1 day fare for a car should be 24*car fare rate")
    public void calculateFareCarWithMoreThanADayParkingTime(){
        //GIVEN
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (Fare.MILLISECOND_BY_HOUR * 24));//24 hours parking time should give 24 * parking fare per hour
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        ticket.setDiscount(false);

        //WHEN
        fareCalculatorService.calculateFare(ticket);

        //THEN
        assertEquals( (24 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
    }

    @Test
    @DisplayName("Calculate 15 mn fare for a car should be 0")
    public void calculatedFareForLessThan30MinuteShouldBe0(){
        //GIVEN
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (long) (Fare.MILLISECOND_BY_HOUR * 0.25) );//15mn parking time should give 0 fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        ticket.setDiscount(false);

        //WHEN
        fareCalculatorService.calculateFare(ticket);

        //THEN
        assertEquals( 0.0 , ticket.getPrice());
    }

    @Test
    @DisplayName("Calculate 2h fare for a recurring car should be 2*0.95*car fare rate")
    public void calculatedFareForRecurringUserShouldBeDiscounted () {
        //GIVEN
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (Fare.MILLISECOND_BY_HOUR * 2) );//2 hours parking time
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        ticket.setDiscount(true); //Discount = true should reduce the fare by 5%

        //WHEN
        fareCalculatorService.calculateFare(ticket);

        //THEN
        assertEquals( (2 * (1-Fare.REGULAR_USER_DISCOUNT_RATE) * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
    }
}
