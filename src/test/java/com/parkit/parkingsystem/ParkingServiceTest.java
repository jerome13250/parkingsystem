package com.parkit.parkingsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.DiscountCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.service.SystemDateService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ParkingServiceTest {

  long inTimeMillis;
  long outTimeMillis;
  int discount;
  private static ParkingService parkingService;

  @Mock
  private static InputReaderUtil inputReaderUtil;
  @Mock
  private static ParkingSpotDAO parkingSpotDAO;
  @Mock
  private static TicketDAO ticketDAO;
  @Mock
  private static SystemDateService systemDateService;
  @Mock
  private static DiscountCalculatorService discountCalculatorService;

  @BeforeEach
  private void setUpPerTest() {
   
    //define fixed inTime and outTime:
    inTimeMillis = 1613568958807L;
    outTimeMillis = 1613568958807L + (60 * 60 * 1000); //1h later
    //define discount %
    discount = 25;

    //ParkingService construction:
    parkingService = new ParkingService(
        inputReaderUtil,
        parkingSpotDAO, 
        ticketDAO, 
        systemDateService,
        discountCalculatorService);
    
  } 
  
  
  @Test
  void getNextParkingNumberIfAvailableTest_souldReturnTypeCarId999() {
    //GIVEN
    when(inputReaderUtil.readSelection()).thenReturn(1);
    when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(999);
    
    //WHEN
    ParkingSpot resultParkingSpot = parkingService.getNextParkingNumberIfAvailable();
    
    //THEN
    assertEquals(
        ParkingType.CAR,
        resultParkingSpot.getParkingType(),
        "Mocked inputReaderUtil has returned 1, so ParkingType.CAR is expected");
    assertEquals(
        999,
        resultParkingSpot.getId(),
        "Mocked parkingSpotDAO has returned 999, so ParkingSpot id must be 999");
  }
  
  @Test
  void getNextParkingNumberIfAvailableTest_souldReturnTypeBikeId555() {
    //GIVEN
    when(inputReaderUtil.readSelection()).thenReturn(2);
    when(parkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE)).thenReturn(555);
    
    //WHEN
    ParkingSpot resultParkingSpot = parkingService.getNextParkingNumberIfAvailable();
    
    //THEN
    assertEquals(
        ParkingType.BIKE,
        resultParkingSpot.getParkingType(),
        "Mocked inputReaderUtil has returned 2, so ParkingType.BIKE is expected");
    assertEquals(
        555,
        resultParkingSpot.getId(),
        "Mocked parkingSpotDAO has returned 555, so ParkingSpot id must be 555");
  }
  
  @Test
  void getNextParkingNumberIfAvailableTest_ParkingSlotsFull_shouldReturnNull() {
    //GIVEN
    when(inputReaderUtil.readSelection()).thenReturn(1);
    when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(0); //0 no slot available
    
    //WHEN
    ParkingSpot resultParkingSpot = parkingService.getNextParkingNumberIfAvailable();
    
    //THEN
    assertNull(
        resultParkingSpot,
        "Mocked parkingSpotDAO has returned 0, meaning no empty spot. "
        + "resultParkingSpot must be null");
  }
  
  @Test
  void getNextParkingNumberIfAvailableTest_InvalidInputReader_souldReturnNull() {
    //GIVEN
    when(inputReaderUtil.readSelection()).thenReturn(3);
        
    //WHEN
    ParkingSpot resultParkingSpot = parkingService.getNextParkingNumberIfAvailable();
    
    //THEN
    assertNull(
        resultParkingSpot,
        "Invalid Input User. resultParkingSpot must be null");
  }
  

  @Test
  void processIncomingVehicleTest() {
    
    try {
      //GIVEN
      //Mock systemDateService:
      when(systemDateService.getCurrentDate()).thenReturn(new Date(inTimeMillis));
      //Mock inputReaderUtil:
      when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
      when(inputReaderUtil.readSelection()).thenReturn(1);
      //Mock parkingSpotDAO:
      ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
      when(parkingSpotDAO.updateParking(parkingSpot)).thenReturn(true);
      when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
      //Mock discountCalculatorService:
      when(discountCalculatorService.calculateDiscount(any(Ticket.class))).thenReturn(discount);
      
    } catch (Exception e) {
      e.printStackTrace();
    } 

    //WHEN
    parkingService.processIncomingVehicle();

    //THEN
    //Ticket update:
    ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
    verify(ticketDAO, Mockito.times(1)).saveTicket(ticketCaptor.capture());
    Ticket ticket = ticketCaptor.getValue();
    assertEquals(
        discount,
        ticket.getDiscountPercentage(),
        "ticket discount value must be 25 coming from mocked discountCalculatorService");
    assertEquals(
        new Date(inTimeMillis),
        ticket.getInTime(),
        "ticket inTime value must be 1613568958807 coming from mocked systemDateService");
    assertNull(
        ticket.getOutTime(),
        "Entering Parking so OutTime must be null");
    assertEquals(
        0,
        ticket.getPrice(),
        "Must be 0 since fare can only be calculated when exiting parking");
    assertEquals(
        "ABCDEF",
        ticket.getVehicleRegNumber(),
        "ticket vehicleRegNumber must be ABCDEF coming from mocked inputReaderUtil");
    
    //Parking update:
    ArgumentCaptor<ParkingSpot> parkingSpotCaptor = ArgumentCaptor.forClass(ParkingSpot.class);
    verify(parkingSpotDAO, Mockito.times(1)).updateParking(parkingSpotCaptor.capture());
    ParkingSpot parkingSpot = parkingSpotCaptor.getValue();
    assertEquals(
        1,
        parkingSpot.getId(),
        "parking spot id must be 1, coming from mocked "
        + "parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)");
    assertEquals(
        ParkingType.CAR,
        parkingSpot.getParkingType(),
        "parking spot type must be CAR, coming from mocked "
        + "inputReaderUtil.readSelection()).thenReturn(1)");
    assertFalse(
        parkingSpot.isAvailable(),
        "Car is entering so parking Spot 1 must be set unavailable");
  } 


  @Test
  void processExitingVehicleTest() {
 
    try {
      //GIVEN
      //Mock systemDateService:
      when(systemDateService.getCurrentDate()).thenReturn(new Date(outTimeMillis));
      //Mock inputReaderUtil:
      when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
      //Mock parkingSpotDAO:
      ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
      when(parkingSpotDAO.updateParking(parkingSpot)).thenReturn(true);
      //Mock ticketDAO:
      Ticket ticket = new Ticket();
      ticket.setId(123456);
      ticket.setInTime(new Date(inTimeMillis));
      ticket.setVehicleRegNumber("ABCDEF");
      ticket.setDiscountPercentage(discount);
      ticket.setParkingSpot(parkingSpot);
      when(ticketDAO.getTicket("ABCDEF")).thenReturn(ticket);
      when(ticketDAO.updateTicket(ticket)).thenReturn(true);

    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to set up test mock objects");
    } 

    //WHEN
    parkingService.processExitingVehicle();

    //THEN
    ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
    verify(ticketDAO, Mockito.times(1)).updateTicket(ticketCaptor.capture());
    Ticket ticket = ticketCaptor.getValue();
    assertEquals(
        123456,
        ticket.getId(),
        "ticket id value must be 123456 coming from mocked ticketDAO.getTicket");
    assertEquals(
        new Date(inTimeMillis),
        ticket.getInTime(),
        "ticket inTime value must be " + inTimeMillis + " coming from mocked ticketDAO.getTicket");
    assertEquals(
        "ABCDEF",
        ticket.getVehicleRegNumber(),
        "ticket vehicleRegNumber must be ABCDEF coming from mocked ticketDAO.getTicket");
    assertEquals(
        discount,
        ticket.getDiscountPercentage(),
        "ticket discount value must be " + discount + " coming from mocked ticketDAO.getTicket");
    assertEquals(
        new Date(outTimeMillis),
        ticket.getOutTime(),
        "ticket outTime value must be " + outTimeMillis + " coming from "
            + "mocked systemDateService.getCurrentDate");
    assertEquals(
        Fare.CAR_RATE_PER_HOUR * (100 - discount) / 100,
        ticket.getPrice(),
        //TODO : Ajouter le commentaire apres passage de fareCalculatorService en injection de dependance
        "");
    
    
    ArgumentCaptor<ParkingSpot> parkingSpotCaptor = ArgumentCaptor.forClass(ParkingSpot.class);
    verify(parkingSpotDAO, Mockito.times(1)).updateParking(parkingSpotCaptor.capture());
    ParkingSpot parkingSpot = parkingSpotCaptor.getValue();
    assertEquals(
        1,
        parkingSpot.getId(),
        "Parking Spot id 1 must be freed, value coming from mocked ticketDAO.getTicket");
    assertEquals(
        ParkingType.CAR,
        parkingSpot.getParkingType(),
        "Parking Spot type is CAR, value coming from mocked ticketDAO.getTicket");
    assertTrue(
        parkingSpot.isAvailable(),
        "Car is exiting so parking Spot 1 must be set available");
  } 
  
} 
