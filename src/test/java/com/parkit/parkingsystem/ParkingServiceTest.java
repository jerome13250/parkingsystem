package com.parkit.parkingsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
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
public class ParkingServiceTest {

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
  public void processIncomingVehicle() {
    
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
    ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
    verify(ticketDAO, Mockito.times(1)).saveTicket(ticketCaptor.capture());
    Ticket ticket = ticketCaptor.getValue();
    assertEquals(discount, ticket.getDiscountPercentage());
    assertEquals(new Date(inTimeMillis), ticket.getInTime());
    assertEquals(null, ticket.getOutTime());
    assertEquals(0, ticket.getPrice());
    assertEquals("ABCDEF", ticket.getVehicleRegNumber());
    
    ArgumentCaptor<ParkingSpot> parkingSpotCaptor = ArgumentCaptor.forClass(ParkingSpot.class);
    verify(parkingSpotDAO, Mockito.times(1)).updateParking(parkingSpotCaptor.capture());
    ParkingSpot parkingSpot = parkingSpotCaptor.getValue();
    assertEquals(1, parkingSpot.getId());
    assertEquals(ParkingType.CAR, parkingSpot.getParkingType());
    assertEquals(false, parkingSpot.isAvailable());
  } 


  @Test
  public void processExitingVehicleTest() {
 
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
      ticket.setInTime(new Date(inTimeMillis));
      ticket.setParkingSpot(parkingSpot);
      ticket.setVehicleRegNumber("ABCDEF");
      ticket.setDiscountPercentage(discount);
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
    assertEquals(discount, ticket.getDiscountPercentage());
    assertEquals(new Date(inTimeMillis), ticket.getInTime());
    assertEquals(new Date(outTimeMillis), ticket.getOutTime());
    assertEquals(Fare.CAR_RATE_PER_HOUR * (100 - discount) / 100, ticket.getPrice());
    assertEquals("ABCDEF", ticket.getVehicleRegNumber());
    
    ArgumentCaptor<ParkingSpot> parkingSpotCaptor = ArgumentCaptor.forClass(ParkingSpot.class);
    verify(parkingSpotDAO, Mockito.times(1)).updateParking(parkingSpotCaptor.capture());
    ParkingSpot parkingSpot = parkingSpotCaptor.getValue();
    assertEquals(1, parkingSpot.getId());
    assertEquals(ParkingType.CAR, parkingSpot.getParkingType());
    assertEquals(true, parkingSpot.isAvailable());
    
  } 



} 
