package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;
import java.util.Date;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ParkingService {

  private static final Logger logger = LogManager.getLogger("ParkingService");

  private static FareCalculatorService fareCalculatorService = new FareCalculatorService();

  private InputReaderUtil inputReaderUtil;
  private ParkingSpotDAO parkingSpotDAO;
  private TicketDAO ticketDAO;
  private SystemDateService systemDateService;
  private DiscountCalculatorService discountCalculatorService;

  /**
   * Constructor for the ParkingService class.
   *
   * @param inputReaderUtil Input reader to get information from client
   * @param parkingSpotDAO DAO for database access to ParkingSpot objects
   * @param ticketDAO DAO for database access to Ticket objects
   * @param systemDateService Make the clock a service to allow easier unit testing
   * @param discountCalculatorService calculate the discount to apply to a specific Ticket
   *
   * @see InputReaderUtil
   * @see ParkingSpotDAO
   * @see TicketDAO
   * @see SystemDateService
   * @see DiscountCalculatorService
   * 
   */
  public ParkingService(InputReaderUtil inputReaderUtil,
      ParkingSpotDAO parkingSpotDAO,
      TicketDAO ticketDAO,
      SystemDateService systemDateService,
      DiscountCalculatorService discountCalculatorService) {
    this.inputReaderUtil = inputReaderUtil;
    this.parkingSpotDAO = parkingSpotDAO;
    this.ticketDAO = ticketDAO;
    this.systemDateService = systemDateService;
    this.discountCalculatorService = discountCalculatorService;
  } 
  
  /**
   * Process a vehicle that requires entering the parking.
   * 
   * <p> The process follows the following steps:</p>
   * <ol>
   * <li>Check that a parking spot is available for the vehicle type</li>
   * <li>Reserve the parking spot in the database</li>
   * <li>Create a Ticket object with the required informations</li>
   * <li>Allocate a 5% discount to the Ticket if the vehicle is already known</li>
   * <li>Save the Ticket object in the database</li>
   * </ol>
   * 
   */
  public void processIncomingVehicle() {
    try {
      ParkingSpot parkingSpot = getNextParkingNumberIfAvailable();
      if (parkingSpot != null && parkingSpot.getId() > 0) {

        parkingSpot.setAvailable(false);
        //allot this parking space and mark it's availability as false:
        parkingSpotDAO.updateParking(parkingSpot); 

        Date inTime = systemDateService.getCurrentDate();
        Ticket ticket = new Ticket();
        String vehicleRegNumber = getVehichleRegNumber();
        //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME, DISCOUNT_PC)
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber(vehicleRegNumber);
        ticket.setPrice(0);
        ticket.setInTime(inTime);
        //if vehicleRegNumber already present in db, then set a 5% discount:
        ticket.setDiscountPercentage(
            discountCalculatorService.calculateDiscount(ticket));

        ticketDAO.saveTicket(ticket);
        System.out.println("Generated Ticket and saved in DB");
        System.out.println("Please park your vehicle in spot number:" + parkingSpot.getId());
        System.out.println("Recorded in-time for vehicle number:" 
            + vehicleRegNumber + " is:" + inTime);
      } 
    } catch (Exception e) {
      logger.error("Unable to process incoming vehicle", e);
    } 
  } 

  
  /**
   * Use the InputReaderUtil to request the vehicle registration number to the parking user.
   *
   * @return String object that is the vehicle registration number.
   * 
   * @see InputReaderUtil
   * 
   */
  private String getVehichleRegNumber() throws Exception {
    System.out.println("Please type the vehicle registration number and press enter key");
    return inputReaderUtil.readVehicleRegistrationNumber();
  } 


  /**
   * Look for a parking spot available according to the vehicle type.
   * The vehicle type is provided by the call to {@code getVehichleType} method. 
   *
   * @return ParkingSpot object.
   * 
   */
  public ParkingSpot getNextParkingNumberIfAvailable() {
    int parkingNumber = 0;
    ParkingSpot parkingSpot = null;
    try {
      ParkingType parkingType = getVehichleType();
      parkingNumber = parkingSpotDAO.getNextAvailableSlot(parkingType);
      if (parkingNumber > 0) {
        parkingSpot = new ParkingSpot(parkingNumber, parkingType, true);
      } else {
        throw new Exception("Error fetching parking number from DB. Parking slots might be full");
      } 
    } catch (IllegalArgumentException ie) {
      logger.error("Error parsing user input for type of vehicle", ie);
    } catch (Exception e) {
      logger.error("Error fetching next available parking slot", e);
    } 
    return parkingSpot;
  } 

  /**
   * Get the vehicle type from  the inputReaderUtil calling {@code readSelection()} method.
   *
   * @return ParkingType object.
   * 
   * @see ParkingType
   * 
   */
  private ParkingType getVehichleType() { 
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
        System.out.println("Incorrect input provided");
        throw new IllegalArgumentException("Entered input is invalid");

    } 
  } 
  
  
  /**
   * Process a vehicle that requires exiting the parking.
   * 
   * <p> The process follows the following steps:</p>
   * <ol>
   * <li>Get the vehicle registration number from inputReaderUtil</li>
   * <li>Get the ticket from database according to registration number</li>
   * <li>Update ticket outTime to the current time</li>
   * <li>Update ticket price using fareCalculatorService</li>
   * <li>Update ticket in database</li>
   * <li>Update the parking spot to available in database</li>
   * </ol>
   * 
   */
  public void processExitingVehicle() {
    try {
      String vehicleRegNumber = getVehichleRegNumber();
      Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
      Date outTime = systemDateService.getCurrentDate();
      ticket.setOutTime(outTime);
      fareCalculatorService.calculateFare(ticket);
      if (ticketDAO.updateTicket(ticket)) {
        ParkingSpot parkingSpot = ticket.getParkingSpot();
        parkingSpot.setAvailable(true);
        parkingSpotDAO.updateParking(parkingSpot);
        System.out.println("Please pay the parking fare:" + ticket.getPrice());
        System.out.println("Recorded out-time for vehicle number:" 
            + ticket.getVehicleRegNumber() + " is:" + outTime);
      } else {
        System.out.println("Unable to update ticket information. Error occurred");
      } 
    } catch (Exception e) {
      logger.error("Unable to process exiting vehicle", e);
    } 
  } 
} 
