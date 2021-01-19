package com.parkit.parkingsystem.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Scanner;

/**
 * Used to retrieve the data entered by the user.
 */
public class InputReaderUtil {

    private static Scanner scan = new Scanner(System.in, "UTF-8");
    private static final Logger LOGGER
            = LogManager.getLogger("InputReaderUtil");

    /**
     * Reads the integer entered to choose the line in the proposed menu.
     * @return an integer
     */
    public int readSelection() {
        try {
            return Integer.parseInt(scan.nextLine());
        } catch (Exception e) {
            LOGGER.error("Error while reading user input from Shell", e);
            System.out.println("Error reading input. "
                    + "Please enter valid number for proceeding further");
            return -1;
        }
    }

    /**
     * Reads the registration number entered by the user.
     * @return the registration number
     */
    public String readVehicleRegistrationNumber() {
        try {
            String vehicleRegNumber = scan.nextLine();
            if (vehicleRegNumber == null
                    || vehicleRegNumber.trim().length() == 0) {
                throw new IllegalArgumentException("Invalid input provided");
            }
            return vehicleRegNumber;
        } catch (Exception e) {
            LOGGER.error("Error while reading user input from Shell", e);
            System.out.println("Error reading input. "
              + "Please enter a valid string for vehicle registration number");
            throw e;
        }
    }


}
