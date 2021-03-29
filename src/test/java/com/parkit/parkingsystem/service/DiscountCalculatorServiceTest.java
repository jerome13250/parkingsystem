package com.parkit.parkingsystem.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
  void testCalculateDiscountReturns5SinceAlreadyinDb() {
    //GIVEN
    //new ticket we want to create
    Ticket ticketToCreate = new Ticket();
    ticketToCreate.setVehicleRegNumber("ABCDEF");
    //Mock ticketDAO:
    when(ticketDAO.getTicket("ABCDEF")).thenReturn(new Ticket());
    
    //WHEN
    int discount = discountCalculatorService.calculateDiscount(ticketToCreate);
    
    //THEN
    assertEquals(
        5,
        discount,
        "Discount should be 5 since a ticket with same reg number already exists in DB");
  }
  
  @Test
  void testCalculateDiscountReturns0SinceNotinDb() {
    //GIVEN
    //new ticket we want to create
    Ticket ticketToCreate = new Ticket();
    ticketToCreate.setVehicleRegNumber("ABCDEF");
    //Mock ticketDAO:
    when(ticketDAO.getTicket("ABCDEF")).thenReturn(null);
    
    //WHEN
    int discount = discountCalculatorService.calculateDiscount(ticketToCreate);
    
    //THEN
    assertEquals(
        0,
        discount,
        "Discount should be 0 since no ticket with same reg number exists in DB");
  }
}
