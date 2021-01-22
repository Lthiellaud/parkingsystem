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
import org.junit.jupiter.api.TestTemplate;
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
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    private static DataBaseRequestService dataBaseRequestService;
    private static Date refDate;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() {
        parkingSpotDAO = new ParkingSpotDAO(dataBaseTestConfig);
        ticketDAO = new TicketDAO(dataBaseTestConfig);
        dataBasePrepareService = new DataBasePrepareService();
        dataBaseRequestService = new DataBaseRequestService();
    }

    @BeforeEach
    private void setUpPerTest() {
        dataBasePrepareService.clearDataBaseEntries();
        refDate = new Date (System.currentTimeMillis() - 1000);
    }

    @Test
    public void testParkingACar() {
        //GIVEN
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

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
        assertThat(ticketDAO.checkRecurringUser(ticket)).isFalse();
    }

    @Test
    public void testTryParkingACar_ParkingFull() {
        //GIVEN
        when(inputReaderUtil.readSelection()).thenReturn(1);
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        dataBaseRequestService.fillParking();

        //WHEN
        parkingService.processIncomingVehicle();

        //THEN
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertThat(ticket).isNull();
    }

    @Test
    public void testParkingLotExit() {

        //GIVEN
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        dataBaseRequestService.occupyParking(1);
        dataBaseRequestService.createTickets(true, "ABCDEF");

        //WHEN
        parkingService.processExitingVehicle();
        Ticket ticket = dataBaseRequestService.getExitedVehicleTicket("ABCDEF");
        ticket.setDiscount(ticketDAO.checkRecurringUser(ticket));
        int result = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);

        //THEN
        double saved_price_low = (FREE_TIME*60*60*1000+299000) / MILLISECOND_BY_HOUR * Fare.CAR_RATE_PER_HOUR;
        double saved_price_high = (FREE_TIME*60*60*1000+301000) /MILLISECOND_BY_HOUR * Fare.CAR_RATE_PER_HOUR;
        assertThat(result).isEqualTo(1);
        assertThat(ticket.getPrice()).isBetween(saved_price_low, saved_price_high);
        assertThat(ticket.getOutTime()).isBetween(refDate, new Date(System.currentTimeMillis()+1000),
                true,true);
        assertThat(ticket.getInTime()).isBefore(ticket.getOutTime());
    }

    @Test
    public void testParkingLotExitOfRecurringUser() {

        //GIVEN
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        dataBaseRequestService.occupyParking(1);
        dataBaseRequestService.createTickets(false, "ABCDEF");
        dataBaseRequestService.createTickets(true, "ABCDEF");

        //WHEN
        parkingService.processExitingVehicle();
        Ticket ticket = dataBaseRequestService.getExitedVehicleTicket("ABCDEF");
        ticket.setDiscount(ticketDAO.checkRecurringUser(ticket));

        //THEN
        assertThat(ticket.getDiscount()).isTrue();
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
