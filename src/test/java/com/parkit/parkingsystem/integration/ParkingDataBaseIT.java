package com.parkit.parkingsystem.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.DiscountCalculatorService;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.service.SystemDateService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import java.util.Date;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class ParkingDataBaseIT {

  Long systemCurrentTimeMillis;
  Long systemCurrentTimeMillisPlusOneHour;
  
  private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
  private static FareCalculatorService fareCalculatorService;
  private static ParkingSpotDAO parkingSpotDAO;
  private static TicketDAO ticketDAO;
  private static DataBasePrepareService dataBasePrepareService;
  private static DiscountCalculatorService discountCalculatorService;

  @Mock
  private static InputReaderUtil inputReaderUtil;

  @Mock
  private static SystemDateService systemDateService;

  @BeforeAll
  private static void setUp() throws Exception {
    fareCalculatorService = new FareCalculatorService();
    parkingSpotDAO = new ParkingSpotDAO();
    parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
    ticketDAO = new TicketDAO();
    ticketDAO.dataBaseConfig = dataBaseTestConfig;
    dataBasePrepareService = new DataBasePrepareService();
    discountCalculatorService = new DiscountCalculatorService(ticketDAO);
  } 

  @BeforeEach
  private void setUpPerTest() throws Exception {
    //Mock inputReaderUtil settings:
    when(inputReaderUtil.readSelection()).thenReturn(1);
    when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
    //Mock systemDateService settings:
    systemCurrentTimeMillis = System.currentTimeMillis();
    systemCurrentTimeMillisPlusOneHour =  systemCurrentTimeMillis + 3600 * 1000;
    when(systemDateService.getCurrentDate())
    //first call will return the current time (for inTime value):
    .thenReturn(new Date(systemCurrentTimeMillis))
        //2nd call will return the current time + 1 hour (for outTime value):
        .thenReturn(new Date(systemCurrentTimeMillisPlusOneHour)); 
    //clean database:
    dataBasePrepareService.clearDataBaseEntries();
  } 

  @AfterAll
  private static void tearDown() {

  } 

  @Test
  void testParkingLotEnter() {
    //GIVEN
    ParkingService parkingService = new ParkingService(
        fareCalculatorService,
        inputReaderUtil,
        parkingSpotDAO,
        ticketDAO,
        systemDateService,
        discountCalculatorService);
    //WHEN
    parkingService.processIncomingVehicle();

    //THEN
    //get the Ticket that has been saved in the database:
    Ticket resultTicket = ticketDAO.getTicket("ABCDEF"); 

    //check that the ticket in database has the correct values:
    //id
    assertEquals(1, resultTicket.getId(), "Must be 1 since db was empty"); 
    //vehicleRegNumber
    assertEquals("ABCDEF", resultTicket.getVehicleRegNumber(),
        "must be equal to mocked ticketDAO.getTicket value");
    //price
    assertEquals(0, resultTicket.getPrice(), "Must be 0 since we are entering parking"); 
    //parkingSpot
    assertEquals(
        1,
        resultTicket.getParkingSpot().getId(),
        "must be 1 since it is the first slot available for TYPE=CAR in table parking");
    //inTime :  fractional second in our MySQL DATETIME is default so precision is 0.
    //This means stored Time can be different from input time up to 1 second (1000 ms) :
    assertEquals(
        systemCurrentTimeMillis,
        resultTicket.getInTime().getTime(),
        1000, //delta 1s (1000 ms)
        "InTime date stored in database ticket must be the one provided "
        + "by mocked systemDateService with 1s delta allowed");
    //outTime
    assertNull(resultTicket.getOutTime(), "OutTime must be null since we did not exit parking"); 
    //discountPercentage
    assertEquals(0, resultTicket.getDiscountPercentage(), "Must be 0 since first entry in db");
    
    //check that Parking table is updated with availability:
    assertEquals(
        2,
        parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR),
        "Since we are using slot1, the next available slot in database must be 2"); 
  } 

  @Test
  void testParkingLotExit() {
    //GIVEN
    testParkingLotEnter();  
    ParkingService parkingService = new ParkingService(
        fareCalculatorService,
        inputReaderUtil,
        parkingSpotDAO, 
        ticketDAO, 
        systemDateService,
        discountCalculatorService);
    //WHEN
    parkingService.processExitingVehicle();
    //THEN
    //get the Ticket that has been saved in the database:
    Ticket resultTicket = ticketDAO.getTicket("ABCDEF"); 
    //Check outTime value :  fractional second in our MySQL DATETIME is default so precision is 0.
    //This means stored Time can be different from input time up to 1 second (1000 ms) :
    assertEquals(
        systemCurrentTimeMillisPlusOneHour,
        resultTicket.getOutTime().getTime(),
        1000, //delta 1s (1000 ms)
        "OutTime date stored in database ticket must be the one provided "
            + "by mocked systemDateService with 1s delta allowed");
    //check the fare for 1 hour CAR parking: 
    //note that inTime comes from database so it is truncated to 1 second precision.
    //outTime comes from real Date so 1ms precision, this induces a small delta in price calculation
    assertEquals(
        Fare.CAR_RATE_PER_HOUR,
        resultTicket.getPrice(),
        0.01, //price delta
        "For 1hour parking car, fare must be Fare.CAR_RATE_PER_HOUR, "
        + "1 centime delta allowed due to MySQL DATETIME precision");
    
    //check that Parking table is updated with availability:
    assertEquals(
        1,
        parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR),
        "Since we have freed slot1, the next available slot must be 1"); 

  } 

} 
