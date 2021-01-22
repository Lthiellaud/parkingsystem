package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    private void setUpSystemOut() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    private void setToStandardSystemOut() {
        System.setOut(standardOut);
    }

    private void setUpExiting() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");

            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            when(ticketDAO.checkRecurringUser(any(Ticket.class))).thenReturn(false);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    private void setUpIncoming() {
        Ticket ticket = new Ticket();
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            when(inputReaderUtil.readSelection()).thenReturn(1);

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,true);
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");

            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(false);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }

    }

    @Test
    public void processExitingVehicle_updateOk_Test(){
        //GIVEN
        setUpExiting();
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

        //WHEN
        parkingService.processExitingVehicle();

        //THEN
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).checkRecurringUser(any(Ticket.class));
        verify(ticketDAO, Mockito.times(1)).updateTicket(any(Ticket.class));
        verify(ticketDAO, Mockito.times(1)).getTicket(anyString());
        assertThat(outputStreamCaptor.toString()).containsOnlyOnce("Recorded out-time for vehicle number");
    }

    @Test
    public void processExitingVehicle_updateKO_Test(){
        //GIVEN
        setUpExiting();
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

        //WHEN
        parkingService.processExitingVehicle();

        //THEN
        verify(ticketDAO, Mockito.times(1)).checkRecurringUser(any(Ticket.class));
        verify(ticketDAO, Mockito.times(1)).updateTicket(any(Ticket.class));
        verify(ticketDAO, Mockito.times(1)).getTicket(anyString());
        assertThat(outputStreamCaptor.toString()).containsOnlyOnce("Unable to update ticket information. Error occurred");
    }

    @Test
    public void processExitingVehicle_getTicketKO_Test(){
        //GIVEN
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDE");
        when(ticketDAO.getTicket(anyString())).thenReturn(null);
        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        //WHEN
        parkingService.processExitingVehicle();

        //THEN
        assertThat(outputStreamCaptor.toString()).containsOnlyOnce("Unable to get your ticket. Please check your registration number");
        verify(ticketDAO, Mockito.times(1)).getTicket(anyString());
        verify(ticketDAO, Mockito.times(0)).checkRecurringUser(any(Ticket.class));
    }

    @Test
    public void processIncomingVehicle_withDiscount_Test(){
        //GIVEN
        setUpIncoming();
        when(ticketDAO.checkRecurringUser(any(Ticket.class))).thenReturn(true);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);

        //WHEN
        parkingService.processIncomingVehicle();

        //THEN
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).checkRecurringUser(any(Ticket.class));
        verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
        assertThat(outputStreamCaptor.toString()).containsOnlyOnce("recurring");
    }

    @Test
    public void processIncomingVehicle_withoutDiscount_Test(){
        //GIVEN
        setUpIncoming();
        when(ticketDAO.checkRecurringUser(any(Ticket.class))).thenReturn(false);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);

        //WHEN
        parkingService.processIncomingVehicle();

        //THEN
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).checkRecurringUser(any(Ticket.class));
        verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
        assertThat(outputStreamCaptor.toString()).doesNotContain("recurring");
    }

    @Test
    public void getVehicleTypeBikeTest() {
       //GIVEN
       when(inputReaderUtil.readSelection()).thenReturn(2);
       when(parkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE)).thenReturn(4);
       parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

       //WHEN
       ParkingType parkingType = parkingService.getNextParkingNumberIfAvailable().getParkingType();

       //THEN
       assertThat(parkingType).isEqualTo(ParkingType.BIKE);

   }

    @Test
    //@Disabled
    public void processIncomingVehicle_ParkingSlotFullTest() {
        //GIVEN
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(-1);
        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        //WHEN
        parkingService.processIncomingVehicle();

        //THEN
        verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(0)).checkRecurringUser(any(Ticket.class));
        assertThat(outputStreamCaptor.toString()).doesNotContain("Generated Ticket and saved in DB");
        assertThat(outputStreamCaptor.toString()).contains("You cannot enter. Parking slots are full");
    }

    @Test
    public void processIncomingVehicle_incorrectVehicleTypeTest() {
        //GIVEN
        when(inputReaderUtil.readSelection()).thenReturn(8);
        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        //WHEN
        parkingService.processIncomingVehicle();

        //THEN
        verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(0)).checkRecurringUser(any(Ticket.class));
        assertThat(outputStreamCaptor.toString()).doesNotContain("Generated Ticket and saved in DB");
        assertThat(outputStreamCaptor.toString()).contains("That's not a correct input");
    }

}
