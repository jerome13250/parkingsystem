package com.parkit.parkingsystem.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.DiscountCalculatorService;
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
  public void testParkingCar() {
    //GIVEN
    ParkingService parkingService = new ParkingService(
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

    //check that a ticket is actually saved in DB
    //must be 1 since DB is empty:
    assertEquals(1, resultTicket.getId()); 
    //must be 1 since it is the first slot available for TYPE=CAR in table parking:
    assertEquals(1, resultTicket.getParkingSpot().getId());
    //must be 0 since outTime is unknown
    assertEquals(0, resultTicket.getPrice()); 
    //Check inTime value :  fractional second in our MySQL DATETIME is default so precision is 0.
    //This means stored Time can be different from input time up to 1 second (1000 ms) :
    assertTrue(Math.abs(systemCurrentTimeMillis - resultTicket.getInTime().getTime()) < 1000);
    //must be null since outTime is unknown :
    assertEquals(null, resultTicket.getOutTime()); 

    //check that Parking table is updated with availability:
    //Since we are using slot1, the next available slot must be 2
    assertEquals(2, parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)); 
  } 

  @Test
  public void testParkingLotExit() {
    //GIVEN
    testParkingCar();  
    ParkingService parkingService = new ParkingService(
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
    assertTrue(Math.abs(systemCurrentTimeMillisPlusOneHour - resultTicket.getOutTime().getTime()) < 1000);
    //check the fare for 1 hour CAR parking: 
    //note that inTime comes from database so it is truncated to 1 second precision.
    //outTime comes from real Date so 1ms precision, this induces a very small difference in price calculation
    assertTrue(Math.abs(Fare.CAR_RATE_PER_HOUR - resultTicket.getPrice()) < 0.01);
    
    //check that Parking table is updated with availability:
    //Since we are freeing slot1, the next available slot must be 1
    assertEquals(1, parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)); 

  } 

} 
