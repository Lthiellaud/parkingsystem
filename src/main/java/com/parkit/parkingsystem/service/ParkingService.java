package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;

import static com.parkit.parkingsystem.constants.Fare.FREE_TIME;

/**
 * handles all actions related to parking lot entrances and exits. *
 */
public class ParkingService {

    private static final Logger LOGGER = LogManager.getLogger("ParkingService");

    private static FareCalculatorService fareCalculatorService
            = new FareCalculatorService();

    private InputReaderUtil inputReaderUtil;
    private ParkingSpotDAO parkingSpotDAO;
    private TicketDAO ticketDAO;

    /**
     * ParkingService constructor.
     * @param inputReaderUtil Gives the data sent by the user
     * @param parkingSpotDAO Class used for retrieving data from table "parking"
     * @param ticketDAO Class used for retrieving data from table "ticket"
     */
    public ParkingService(final InputReaderUtil inputReaderUtil,
                          final ParkingSpotDAO parkingSpotDAO,
                          final TicketDAO ticketDAO) {
        this.inputReaderUtil = inputReaderUtil;
        this.parkingSpotDAO = parkingSpotDAO;
        this.ticketDAO = ticketDAO;
    }

    /**
     * handles with parking lot entrances.
     *  Checks if there are available parking lot, and if it's ok,
     *  allocate a specific parking place to the user and mark it as occupied.
     *  Also checks if he is a recurrent user to tell him
     *  if he will benefit from a 5% discount or not
     */
    public void processIncomingVehicle() {
        try {
            ParkingSpot parkingSpot = getNextParkingNumberIfAvailable();
            if (parkingSpot != null && parkingSpot.getId() > 0) {
                String vehicleRegNumber = getVehicleRegNumber();
                parkingSpot.setAvailable(false);
                parkingSpotDAO.updateParking(parkingSpot);
                Date inTime = new Date();
                Ticket ticket = new Ticket();
                ticket.setParkingSpot(parkingSpot);
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(0);
                ticket.setInTime(inTime);
                ticket.setOutTime(null);
                ticket.setDiscount(ticketDAO.checkRecurringUser(ticket));
                ticketDAO.saveTicket(ticket);
                System.out.println("Generated Ticket and saved in DB");
                if (ticket.getDiscount()) {
                    System.out.println("Welcome back! "
                            + "As a recurring user of our parking lot, "
                            + "you'll benefit from a 5% discount.");
                }
                System.out.println("Please park your vehicle in spot "
                        + "number: " + parkingSpot.getId());
                System.out.println("Recorded in-time for vehicle "
                        + "number: " + vehicleRegNumber + " is:" + inTime);
            }
        } catch (Exception e) {
            LOGGER.error("Unable to process incoming vehicle", e);
        }
    }

    /**
     * Asks the user the registration number of his vehicle.
     * @return the vehicle registration number the user has typed on the shell
     *
     */
    private String getVehicleRegNumber() {
        System.out.println("Please type the vehicle registration number "
                + "and press enter key");
        return inputReaderUtil.readVehicleRegistrationNumber();
    }

    /**
     * Checks and gives the next available parking spot.
     * Asks the user the the parking type needed, checks and gives the next
     *  corresponding available parking spot
     * @return the next available parking spot for the asked vehicle type
     */
    public ParkingSpot getNextParkingNumberIfAvailable() {
        int parkingNumber;
        ParkingSpot parkingSpot = null;
        try {
            ParkingType parkingType = getVehicleType();
            parkingNumber = parkingSpotDAO.getNextAvailableSlot(parkingType);
            if (parkingNumber > 0) {
                parkingSpot = new ParkingSpot(parkingNumber, parkingType, true);
            } else {
                System.out.println("You cannot enter. Parking slots are full");
                throw new Exception("Error fetching parking number "
                        + "from DB. Parking slots might be full");
            }
        } catch (IllegalArgumentException ie) {
            LOGGER.error("Error parsing user input for type of vehicle", ie);
        } catch (Exception e) {
            LOGGER.error("Error fetching next available parking slot.", e);
        }
        return parkingSpot;
    }

    /**
     * Asks the user the parking type needed.
     * @return the parking type needed following
     *  the answer given by the user on the shell
     */
    private ParkingType getVehicleType() {
        System.out.println("Please select vehicle type from menu");
        System.out.println("1 CAR");
        System.out.println("2 BIKE");
        int input = inputReaderUtil.readSelection();
        switch (input) {
            case 1:
                return ParkingType.CAR;
            case 2:
                return ParkingType.BIKE;
            default:
                System.out.println("That's not a correct input");
                LOGGER.error("Incorrect input provided");
                throw new IllegalArgumentException("Entered input is invalid");
        }
    }

    /**
     * handles with parking lot exits.
     * Calculates the fare and ask the user the amount to be payed     *
     */
    public void processExitingVehicle() {
        try {
            String vehicleRegNumber = getVehicleRegNumber();
            Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
            if (ticket != null) {
                Date outTime = new Date();
                ticket.setOutTime(outTime);
                fareCalculatorService.calculateFare(ticket);
                if (ticketDAO.updateTicket(ticket)) {
                    ParkingSpot parkingSpot = ticket.getParkingSpot();
                    parkingSpot.setAvailable(true);
                    parkingSpotDAO.updateParking(parkingSpot);
                    if (ticket.getPrice() == 0) {
                        System.out.println("You've been parked less than "
                                + (int) (FREE_TIME * 60) + "mn. Nothing to pay !");
                    } else {
                        System.out.println("Please pay the parking fare:"
                                + ticket.getPrice());
                    }
                    System.out.println("Recorded out-time for vehicle number:"
                            + ticket.getVehicleRegNumber() + " is:" + outTime);
                } else {
                    System.out.println("Unable to update ticket information. "
                            + "Error occurred");
                }
            } else {
                System.out.println("Unable to get your ticket. "
                        + "Please check your registration number");
            }
        } catch (Exception e) {
            LOGGER.error("Unable to process exiting vehicle", e);
        }
    }
}
