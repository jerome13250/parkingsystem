package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InteractiveShell {

  private static final Logger logger = LogManager.getLogger("InteractiveShell");

  /**
   * Initialize all required objects to run the application. 
   */
  public static void loadInterface() {
    logger.info("App initialized!!!");
    System.out.println("Welcome to Parking System!");

    boolean continueApp = true;
    FareCalculatorService fareCalculatorService = new FareCalculatorService();
    InputReaderUtil inputReaderUtil = new InputReaderUtil();

    DataBaseConfig dataBaseConfig = new DataBaseConfig();
    ParkingSpotDAO parkingSpotDAO = new ParkingSpotDAO(dataBaseConfig);
    TicketDAO ticketDAO = new TicketDAO(dataBaseConfig);
    
    SystemDateService systemDateService = new SystemDateService();
    DiscountCalculatorService discountCalculatorService = new DiscountCalculatorService(ticketDAO);
    
    ParkingService parkingService = new ParkingService(
        fareCalculatorService,
        inputReaderUtil, 
        parkingSpotDAO, 
        ticketDAO, 
        systemDateService,
        discountCalculatorService);

    while (continueApp) {
      loadMenu();
      int option = inputReaderUtil.readSelection();
      switch (option) {
        case 1: 
          parkingService.processIncomingVehicle();
          break;

        case 2: 
          parkingService.processExitingVehicle();
          break;

        case 3: 
          System.out.println("Exiting from the system!");
          continueApp = false;
          break;

        default: System.out.println("Unsupported option."
            + " Please enter a number corresponding to the provided menu");
      } 
    } 
  } 

  private static void loadMenu() {
    System.out.println("Please select an option. Simply enter the number to choose an action");
    System.out.println("1 New Vehicle Entering - Allocate Parking Space");
    System.out.println("2 Vehicle Exiting - Generate Ticket Price");
    System.out.println("3 Shutdown System");
  } 

} 
