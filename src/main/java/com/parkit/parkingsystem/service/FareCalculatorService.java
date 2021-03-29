package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;


/**
 * FareCalculatorService is the class that contains the logic to calculate the parking Fare.
 *
 */
public class FareCalculatorService {

  /**
   * Calculate the parking fare for a specific ticket and update it in {@code Ticket.fare}.
   *
   * @param ticket the ticket object to update.
   * 
   * @return  the calculated price
   *
   * @see Ticket
   * 
   */
  public double calculateFare(Ticket ticket) {
    if (ticket.getOutTime() == null) {
      throw new IllegalArgumentException("Out time provided is null");
    } 
    if (ticket.getOutTime().before(ticket.getInTime())) {
      throw new IllegalArgumentException("Out time (" 
         + ticket.getOutTime().toString()
         + ") is inferior to In Time ("
         + ticket.getInTime().toString()
         + ")"
    );
    } 

    //Calculate duration between OutTime and InTime in milliseconds :
    long durationInMilliesec = 
        ticket.getOutTime().getTime() - ticket.getInTime().getTime();

    switch (ticket.getParkingSpot().getParkingType()) {
      case CAR:
        return (priceCalc(durationInMilliesec, 
            Fare.CAR_RATE_PER_HOUR, 
            ticket.getDiscountPercentage()));
      case BIKE: 
        return (priceCalc(durationInMilliesec, 
            Fare.BIKE_RATE_PER_HOUR, 
            ticket.getDiscountPercentage()));

      default: throw new IllegalArgumentException("Unkown Parking Type");
    } 
  } 

  private double priceCalc(
      long durationInMilliesec, 
      double fareRatePerHour, 
      int discountPercentage) {
    //Free 30-min parking:
    if (durationInMilliesec < (30 * 60 * 1000)) {
      return 0;
    } 

    return ((double) durationInMilliesec) / (3600 * 1000) 
        * fareRatePerHour * (100 - discountPercentage) / 100;
  } 

} 