package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.Ticket;

/**
 *  class that contains the logic to calculate the discount to apply to a specific Ticket.
 *
 *@author jerome
 */
public class DiscountCalculatorService {

  TicketDAO ticketDAO;

  /**
   * Constructor for DiscountCalculatorService.
   *
   * @param ticketDAO DAO for ticket, needed to check if user reg number already exists in DB
   */
  public DiscountCalculatorService(TicketDAO ticketDAO) {
    this.ticketDAO = ticketDAO;
  }

  /**
   * Calculate discount for the specified ticket : 5%  if vehicle reg number is already known in db.
   *
   * @param ticket the ticket object to check for discount.
   *
   * @return the calculated discount percentage, integer value. Example: return integer 5 for a 5% discount.
   *
   * @see Ticket
   * 
   */
  public int calculateDiscount(Ticket ticket) {

    if (existTicketInDatabase(ticket.getVehicleRegNumber())) {
      System.out.println("Welcome back! As a recurring user of our parking lot, "
          + "you'll benefit from a 5% discount.");
      return 5;
    } 

    return 0;
  }


  private boolean existTicketInDatabase(String vehicleRegNumber) {

    return (ticketDAO.getTicket(vehicleRegNumber) != null) ? true : false;

  }



}
