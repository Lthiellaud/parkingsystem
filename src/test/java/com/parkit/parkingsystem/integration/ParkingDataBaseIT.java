package com.parkit.parkingsystem.integration;

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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static com.parkit.parkingsystem.constants.Fare.FREE_TIME;
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
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
        dataBaseRequestService = new DataBaseRequestService();
    }

    @BeforeEach
    private void setUpPerTest() {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
        refDate = new Date (System.currentTimeMillis() - 1000);
    }

    @Test
    public void testParkingACar() {
        //GIVEN
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        //WHEN
        parkingService.processIncomingVehicle();

        //THEN
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        int result = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
        assertThat(ticket).isNotNull();
        assertThat(result).isGreaterThan(1);
        assertThat(ticket.getPrice()).isEqualTo(0.0);
        assertThat(ticket.getInTime()).isBetween(refDate, new Date(System.currentTimeMillis()+1000),
                true, true);
        assertThat(ticket.getOutTime()).isNull();
        assertThat(ticketDAO.checkRecurringUser(ticket)).isFalse();
    }

    @Test
    public void testParkingLotExit() {
        //GIVEN
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        parkingSpot.setAvailable(false);
        parkingSpotDAO.updateParking(parkingSpot);
        dataBaseRequestService.createTickets(true, "ABCDEF");
        dataBaseRequestService.createTickets(false, "ABCDEF");

        //WHEN
        parkingService.processExitingVehicle();
        Ticket ticket = dataBaseRequestService.getLastTicket("ABCDEF");
        ticket.setDiscount(ticketDAO.checkRecurringUser(ticket));
        int result = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);

        //THEN

        assertThat(result).isEqualTo(1);
        assertThat(ticket.getPrice()).isGreaterThan(0.0);
        assertThat(ticket.getOutTime()).isBetween(refDate, new Date(System.currentTimeMillis()+1000),
                true,true);
        assertThat(ticket.getInTime()).isBefore(ticket.getOutTime());
        assertThat(ticket.getDiscount()).isTrue();
    }

}
