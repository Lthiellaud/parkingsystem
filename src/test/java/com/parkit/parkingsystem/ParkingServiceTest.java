package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ParkingServiceTest {

    private static ParkingService parkingService;
    private static Date inTime = new Date(System.currentTimeMillis() - Fare.MILLISECOND_BY_HOUR);
    private static Ticket ticket;
    private static ParkingSpot parkingSpot;
    private static Date refDate;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    private void setUpTest() {
        System.setOut(new PrintStream(outputStreamCaptor));
        refDate = new Date (System.currentTimeMillis() - 1000);
        parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        ticket = new Ticket();
        ticket.setInTime(inTime);
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            when(inputReaderUtil.readSelection()).thenReturn(1);
            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @AfterEach
    private void setToStandardSystemOut() {
        System.setOut(standardOut);
    }

    @Test
    @DisplayName("Process exiting vehicle OK")
    public void processExitingVehicle_updateOk_Test(){
        //GIVEN
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

        //WHEN
        parkingService.processExitingVehicle();

        //THEN
        assertThat(parkingSpot.isAvailable()).isTrue();
        assertThat(ticket.getPrice()).isGreaterThan(0);
        assertThat(ticket.getOutTime()).isBetween(refDate, new Date(System.currentTimeMillis()+1000),
                true,true);
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).updateTicket(any(Ticket.class));
        verify(ticketDAO, Mockito.times(1)).getTicket(anyString());
        verify(inputReaderUtil, Mockito.times(1)).readVehicleRegistrationNumber();
        assertThat(outputStreamCaptor.toString()).containsOnlyOnce("Recorded out-time for vehicle number");
    }

    @Test
    @DisplayName("Process exiting vehicle OK ")
    public void processExitingVehicleWith0ToPay(){
        //GIVEN
        ticket.setInTime(new Date(System.currentTimeMillis() - (long) 0.25*Fare.MILLISECOND_BY_HOUR));
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

        //WHEN
        parkingService.processExitingVehicle();

        //THEN
        assertThat(parkingSpot.isAvailable()).isTrue();
        assertThat(ticket.getPrice()).isEqualTo(0);
        assertThat(ticket.getOutTime()).isBetween(refDate, new Date(System.currentTimeMillis()+1000),
                true,true);
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).updateTicket(any(Ticket.class));
        verify(ticketDAO, Mockito.times(1)).getTicket(anyString());
        verify(inputReaderUtil, Mockito.times(1)).readVehicleRegistrationNumber();
        assertThat(outputStreamCaptor.toString()).containsOnlyOnce("Recorded out-time " +
                "for vehicle number");
        assertThat(outputStreamCaptor.toString()).containsOnlyOnce("Nothing to pay !");
    }

    @Test
    @DisplayName("Process exiting vehicle - simulate update table ticket KO")
    public void processExitingVehicle_updateKO_Test(){
        //GIVEN
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

        //WHEN
        parkingService.processExitingVehicle();

        //THEN
        verify(ticketDAO, Mockito.times(1)).updateTicket(any(Ticket.class));
        verify(ticketDAO, Mockito.times(1)).getTicket(anyString());
        assertThat(parkingSpot.isAvailable()).isFalse();
        assertThat(outputStreamCaptor.toString()).containsOnlyOnce("Unable to update ticket "
                + "information. Error occurred");
    }

    @Test
    @DisplayName("Process exiting vehicle - get ticket KO")
    public void processExitingVehicle_getTicketKO_Test(){
        //GIVEN
        when(ticketDAO.getTicket(anyString())).thenReturn(null);

        //WHEN
        parkingService.processExitingVehicle();

        //THEN
        assertThat(outputStreamCaptor.toString()).containsOnlyOnce("Unable to get your ticket." +
                " Please check your registration number");
        verify(ticketDAO, Mockito.times(1)).getTicket(anyString());
    }

    @Test
    @DisplayName("Process incoming vehicle with discount")
    public void processIncomingVehicle_withDiscount_Test(){
        //GIVEN
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(ticketDAO.checkRecurringUser("ABCDEF")).thenReturn(true);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);

        //WHEN
        parkingService.processIncomingVehicle();

        //THEN
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).checkRecurringUser("ABCDEF");
        verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
        assertThat(outputStreamCaptor.toString()).containsOnlyOnce("recurring");
        assertThat(outputStreamCaptor.toString()).contains("Please park your vehicle in spot number: 1");
    }

    @Test
    @DisplayName("Process incoming vehicle without discount")
    public void processIncomingVehicle_withoutDiscount_Test(){
        //GIVEN
        when(ticketDAO.checkRecurringUser("ABCDEF")).thenReturn(false);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(false);

        //WHEN
        parkingService.processIncomingVehicle();

        //THEN
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).checkRecurringUser("ABCDEF");
        verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
        verify(inputReaderUtil, Mockito.times(1)).readVehicleRegistrationNumber();
        assertThat(outputStreamCaptor.toString()).doesNotContain("recurring");
        assertThat(outputStreamCaptor.toString()).contains("Please park your vehicle in spot number: 1");
    }

    @Test
    @DisplayName("Process incoming vehicle - Parking slot full")
    public void processIncomingVehicle_ParkingSlotFullTest() {
        //GIVEN
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(-1);
        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        //WHEN
        parkingService.processIncomingVehicle();

        //THEN
        verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(0)).checkRecurringUser("ABCDEF");
        verify(inputReaderUtil, Mockito.times(0)).readVehicleRegistrationNumber();
        verify(inputReaderUtil, Mockito.times(1)).readSelection();
        assertThat(outputStreamCaptor.toString()).doesNotContain("Generated Ticket and saved in DB");
        assertThat(outputStreamCaptor.toString()).contains("You cannot enter. Parking slots are full");
    }

    @Test
    @DisplayName("Process incoming vehicle - incorrect vehicle type provided")
    public void processIncomingVehicle_incorrectVehicleTypeTest() {
        //GIVEN
        when(inputReaderUtil.readSelection()).thenReturn(8);
        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        //WHEN
        parkingService.processIncomingVehicle();

        //THEN
        verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(0)).checkRecurringUser("ABCDEF");
        assertThat(outputStreamCaptor.toString()).doesNotContain("Generated Ticket and saved in DB");
        assertThat(outputStreamCaptor.toString()).contains("That's not a correct input");
    }

    @Test
    @DisplayName("Test of getVehicleTypeBike")
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

}
