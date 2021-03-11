package com.parkit.parkingsystem.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

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
    Long systemCurrentTimeMillis = System.currentTimeMillis();
    Long systemCurrentTimeMillisPlusOneHour =  systemCurrentTimeMillis + 3600 * 1000;
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
    //In this case we can not know the exact time the inTime value of Ticket is created,
    //so for test purpose i just check the time difference in Ticket is less than 5sec (5000 msec),
    //seems enough margin for database simple write+read :
    assertTrue(Math.abs((new Date()).getTime() - resultTicket.getInTime().getTime()) < 5000);
    assertEquals(null, resultTicket.getOutTime()); //must be null since outTime is unknown

    //check that Parking table is updated with availability:
    //Since we are using slot1, the next available slot must be 2
    assertEquals(2, parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)); 
  } 

  @Test
  public void testParkingLotExit() {
    //GIVEN
    //TODO: un test qui depend d'un autre test, c'est moche, faut il corriger ca ?
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
    //check that the fare generated correctly in db
    //must be different from 0 since ExitingVehicle has triggered fare calculation:
    assertTrue(0 != resultTicket.getPrice());  
    //In this case we can not know the exact time the outTime value of Ticket is created, so for test purpose 
    //i just check the time difference in Ticket is less than 5sec (5000 msec), seems enough margin for database simple write+read :
    assertEquals(resultTicket.getOutTime().getTime() - resultTicket.getInTime().getTime(),
        (60 * 60 * 1000));

  } 

} 
