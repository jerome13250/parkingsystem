package com.parkit.parkingsystem.util;

import java.util.Scanner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * <b>InputReaderUtil is the class that manages the user inputs reading.</b>
 * <p>
 * It gets user answers and convert them to expected format.
 * </p> 
 */
public class InputReaderUtil {

  private static Scanner scan = new Scanner(System.in);
  private static final Logger logger = LogManager.getLogger("InputReaderUtil");

  
  /**
   * Reads the user input in console and converts it to an integer.
   *
   * @return the converted integer.
   * 
   * @throws Exception if the input is invalid.
   * 
   */
  public int readSelection() {
    try {
      int input = Integer.parseInt(scan.nextLine());
      return input;
    } catch (Exception e) {
      logger.error("Error while reading user input from Shell", e);
      System.out.println("Error reading input. Please enter valid number for proceeding further");
      return -1;
    } 
  } 

  /**
   * Reads the user input in console for registration number and converts it to a String.
   *
   * @return the converted String.
   * 
   * @throws Exception if the input is invalid.
   * 
   */
  public String readVehicleRegistrationNumber() throws Exception {
    try {
      String vehicleRegNumber = scan.nextLine();
      if (vehicleRegNumber == null || vehicleRegNumber.trim().length() == 0) {
        throw new IllegalArgumentException("Invalid input provided");
      } 
      return vehicleRegNumber;
    } catch (Exception e) {
      logger.error("Error while reading user input from Shell", e);
      System.out.println("Error reading input. "
          + "Please enter a valid string for vehicle registration number");
      throw e;
    } 
  } 


} 
