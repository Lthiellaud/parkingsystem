package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.integration.service.DataBaseRequestService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static com.parkit.parkingsystem.constants.Fare.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO = new ParkingSpotDAO();
    private static TicketDAO ticketDAO = new TicketDAO();
    private static DataBasePrepareService dataBasePrepareService;
    private static DataBaseRequestService dataBaseRequestService;
    private static Date refDate;
    private static ParkingService parkingService;
    private static double saved_price_low;
    private static double saved_price_high;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() {
        parkingSpotDAO.setDataBaseConfig(dataBaseTestConfig);
        ticketDAO.setDataBaseConfig(dataBaseTestConfig);
        dataBasePrepareService = new DataBasePrepareService();
        dataBaseRequestService = new DataBaseRequestService();
        //in time and out time are stored with a precision of +/- 1s, the calculated price can't be known exactly
        saved_price_low = (FREE_TIME*60*60*1000+299000) / MILLISECOND_BY_HOUR * Fare.CAR_RATE_PER_HOUR;
        saved_price_high = (FREE_TIME*60*60*1000+301000) /MILLISECOND_BY_HOUR * Fare.CAR_RATE_PER_HOUR;
    }

    @BeforeEach
    private void setUpPerTest() {
        dataBasePrepareService.clearDataBaseEntries();
        refDate = new Date (System.currentTimeMillis() - 1000);
        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

    }

    @Test
    public void testParkingACar() {
        //GIVEN
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

        //WHEN
        parkingService.processIncomingVehicle();
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        int result = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);

        //THEN
        assertThat(ticket).isNotNull();
        assertThat(result).isEqualTo(2);
        assertThat(ticket.getPrice()).isEqualTo(0.0);
        assertThat(ticket.getInTime()).isBetween(refDate, new Date(System.currentTimeMillis()+1000),
                true, true);
        assertThat(ticket.getOutTime()).isNull();
        assertThat(ticket.getDiscount()).isFalse();
    }

    @Test
    public void testParkingACarOfARecurringUser() {
        //GIVEN
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBaseRequestService.createTickets(false, "ABCDEF", false);

        //WHEN
        parkingService.processIncomingVehicle();

        //THEN
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        int result = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
        assertThat(ticket).isNotNull();
        assertThat(result).isEqualTo(2);
        assertThat(ticket.getPrice()).isEqualTo(0.0);
        assertThat(ticket.getInTime()).isBetween(refDate, new Date(System.currentTimeMillis()+1000),
                true, true);
        assertThat(ticket.getOutTime()).isNull();
        assertThat(ticket.getDiscount()).isTrue();
    }

    @Test
    public void testTryParkingACar_ParkingFull() {
        //GIVEN
        when(inputReaderUtil.readSelection()).thenReturn(1);
        dataBaseRequestService.fillParking();

        //WHEN
        parkingService.processIncomingVehicle();

        //THEN
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertThat(ticket).isNull();
    }

    @Test
    public void testParkingLotExit() {

        //GIVEN - the car ABCDEF is parked in spot 1 since 35mn
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBaseRequestService.occupyParking(1);
        dataBaseRequestService.createTickets(true, "ABCDEF", false);

        //WHEN - the car exits
        parkingService.processExitingVehicle();
        Ticket ticket = dataBaseRequestService.getExitedVehicleTicket("ABCDEF");
        int result = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);

        //THEN - in the database, the spot 1 is available, the price is between low and high
        // calculated price, ouTime is more or less equal now
        assertThat(result).isEqualTo(1);
        assertThat(ticket.getPrice()).isBetween(saved_price_low, saved_price_high);
        assertThat(ticket.getOutTime()).isBetween(refDate, new Date(System.currentTimeMillis()+1000),
                true,true);
        assertThat(ticket.getInTime()).isBefore(ticket.getOutTime());
    }

    @Test
    public void testParkingLotExitOfRecurringUser() {

        //GIVEN - A car which has used the parking yesterday has come in today 35mn ago
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBaseRequestService.occupyParking(1);
        dataBaseRequestService.createTickets(false, "ABCDEF", false);
        dataBaseRequestService.createTickets(true, "ABCDEF", true);

        //WHEN
        parkingService.processExitingVehicle();
        Ticket ticket = dataBaseRequestService.getExitedVehicleTicket("ABCDEF");

        //THEN - The ticket update is the today ticket and the saved price is discounted
        assertThat(ticket.getDiscount()).isTrue();
        assertThat(ticket.getInTime()).isAfter(new Date(refDate.getTime()-MILLISECOND_BY_HOUR));
        assertThat(ticket.getPrice()).isBetween(saved_price_low*(1-REGULAR_USER_DISCOUNT_RATE),
                                                saved_price_high*(1-REGULAR_USER_DISCOUNT_RATE));
    }

    @Test
    public void updateNonExistingParkingSpotShouldBeKO() {
        //GIVEN
        ParkingSpot parkingSpot = new ParkingSpot(6, ParkingType.CAR, true);

        //WHEN
        boolean response = parkingSpotDAO.updateParking(parkingSpot);

        //THEN
        assertThat(response).isFalse();

    }

    @Test
    public void updateNonExistingTicketSpotShouldBeKO() {
        //GIVEN  -- ticket non saved in database
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
        ticket.setOutTime(new Date(System.currentTimeMillis()));
        ticket.setPrice(0.0);
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setId(1);
        ticket.setDiscount(false);

        //WHEN
        boolean response = ticketDAO.updateTicket(ticket);

        //THEN
        assertThat(response).isFalse();
    }

    @Test
    public void saveTicketWithOutTimeNotNull () {
        //GIVEN
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (int) (FREE_TIME*60*60*1000+300000)));
        ticket.setOutTime(new Date(System.currentTimeMillis()));
        ticket.setPrice(1.0);
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");

        //WHEN
        ticketDAO.saveTicket(ticket);

        //THEN
        ticket = dataBaseRequestService.getExitedVehicleTicket("ABCDEF");
        assertThat(ticket.getOutTime()).isBetween(refDate, new Date(System.currentTimeMillis()+1000),
                true,true);
    }
}
