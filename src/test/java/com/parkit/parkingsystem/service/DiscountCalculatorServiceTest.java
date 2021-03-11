package com.parkit.parkingsystem.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.Ticket;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DiscountCalculatorServiceTest {

  private DiscountCalculatorService discountCalculatorService;

  @Mock
  private TicketDAO ticketDAO;

  @BeforeEach
  private void setUpPerTest() {
    discountCalculatorService = new DiscountCalculatorService(ticketDAO);
  } 


  @Test
  void testCalculateDiscount_returns5SinceAlreadyinDb() {
    //GIVEN
    //new ticket we want to create
    Ticket ticketToCreate = new Ticket();
    ticketToCreate.setVehicleRegNumber("ABCDEF");
    //Mock ticketDAO: returns a Ticket, no matter if empty...
    when(ticketDAO.getTicket("ABCDEF")).thenReturn(new Ticket());
    
    //WHEN
    int discount = discountCalculatorService.calculateDiscount(ticketToCreate);
    
    //THEN
    assertEquals(5, discount);
  }
  
  @Test
  void testCalculateDiscount_returns0SinceNotinDb() {
    //GIVEN
    //new ticket we want to create
    Ticket ticketToCreate = new Ticket();
    ticketToCreate.setVehicleRegNumber("ABCDEF");
    //Mock ticketDAO: returns a Ticket, no matter if empty...
    when(ticketDAO.getTicket("ABCDEF")).thenReturn(null);
    
    //WHEN
    int discount = discountCalculatorService.calculateDiscount(ticketToCreate);
    
    //THEN
    assertEquals(0, discount);
  }
  
  
}
