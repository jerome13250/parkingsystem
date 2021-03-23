package com.parkit.parkingsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import java.util.Date;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class FareCalculatorServiceTest {

  private static FareCalculatorService fareCalculatorService;
  private Ticket ticket;
  long systemCurrentTimeMillis;

  @BeforeAll
  private static void setUp() {
    fareCalculatorService = new FareCalculatorService();
  } 

  @BeforeEach
  private void setUpPerTest() {
    ticket = new Ticket();
    //need to set inTime and outTime with the same initial System time and shift from there
    //otherwise 2 different calls to Date() leads to errors due to delay between each > 1ms.
    systemCurrentTimeMillis = System.currentTimeMillis();

  } 

  @Test
  void calculateFare_outTimeNull_throwsIllegalArgumentException() {
    //GIVEN
    Date inTime = new Date(systemCurrentTimeMillis);
    Date outTime = null; 
    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

    ticket.setInTime(inTime);
    //ticket.setOutTime(outTime);
    ticket.setParkingSpot(parkingSpot);

    //THEN
    assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
  } 


  @Test
  public void calculateFareCar() {
    Date inTime = new Date(systemCurrentTimeMillis - (60 * 60 * 1000)); //Now - 1h
    Date outTime = new Date(systemCurrentTimeMillis); 
    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

    ticket.setInTime(inTime);
    ticket.setOutTime(outTime);
    ticket.setParkingSpot(parkingSpot);
    fareCalculatorService.calculateFare(ticket);
    assertEquals(Fare.CAR_RATE_PER_HOUR, ticket.getPrice());
  } 

  @Test
  public void calculateFareBike() {
    Date inTime = new Date(systemCurrentTimeMillis - (60 * 60 * 1000)); //Now - 1h
    Date outTime = new Date(systemCurrentTimeMillis);
    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

    ticket.setInTime(inTime);
    ticket.setOutTime(outTime);
    ticket.setParkingSpot(parkingSpot);
    fareCalculatorService.calculateFare(ticket);
    assertEquals(Fare.BIKE_RATE_PER_HOUR, ticket.getPrice());
  } 

  @Test
  public void calculateFareUnkownType() {
    Date inTime = new Date(systemCurrentTimeMillis - (60 * 60 * 1000)); //Now - 1h
    Date outTime = new Date(systemCurrentTimeMillis);
    ParkingSpot parkingSpot = new ParkingSpot(1, null, false);

    ticket.setInTime(inTime);
    ticket.setOutTime(outTime);
    ticket.setParkingSpot(parkingSpot);
    assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
  } 

  @Test
  public void calculateFareBikeWithFutureInTime() {
    Date inTime = new Date(systemCurrentTimeMillis + (60 * 60 * 1000)); //Now + 1h
    Date outTime = new Date(systemCurrentTimeMillis);
    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

    ticket.setInTime(inTime);
    ticket.setOutTime(outTime);
    ticket.setParkingSpot(parkingSpot);
    assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
  } 

  @Test
  public void calculateFareBikeWithLessThanOneHourParkingTime() {
    //45 minutes parking time should give 3/4th parking fare:
    Date inTime = new Date(systemCurrentTimeMillis - (45 * 60 * 1000));
    Date outTime = new Date(systemCurrentTimeMillis);
    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

    ticket.setInTime(inTime);
    ticket.setOutTime(outTime);
    ticket.setParkingSpot(parkingSpot);
    fareCalculatorService.calculateFare(ticket);
    assertEquals((0.75 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());
  } 

  @Test
  public void calculateFareBikeLessThan30MinutesParkingTime() {
    //30 minutes or less parking time should be free
    Date inTime = new Date(systemCurrentTimeMillis - (25 * 60 * 1000)); 
    Date outTime = new Date(systemCurrentTimeMillis);
    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

    ticket.setInTime(inTime);
    ticket.setOutTime(outTime);
    ticket.setParkingSpot(parkingSpot);
    fareCalculatorService.calculateFare(ticket);
    assertEquals(0, ticket.getPrice());
  } 

  @Test
  public void calculateFareCarWithLessThanOneHourParkingTime() {
    //45 minutes parking time should give 3/4th parking fare:
    Date inTime = new Date(systemCurrentTimeMillis - (45 * 60 * 1000));
    Date outTime = new Date(systemCurrentTimeMillis);
    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

    ticket.setInTime(inTime);
    ticket.setOutTime(outTime);
    ticket.setParkingSpot(parkingSpot);
    fareCalculatorService.calculateFare(ticket);
    assertEquals((0.75 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
  } 

  @Test
  public void calculateFareCarWithLessThan30MinutesParkingTime() {
    //30 minutes or less parking time should be free
    Date inTime = new Date(systemCurrentTimeMillis - (25 * 60 * 1000)); 
    Date outTime = new Date(systemCurrentTimeMillis);
    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

    ticket.setInTime(inTime);
    ticket.setOutTime(outTime);
    ticket.setParkingSpot(parkingSpot);
    fareCalculatorService.calculateFare(ticket);
    assertEquals(0, ticket.getPrice());
  } 


  @Test
  public void calculateFareCarWithMoreThan24hParkingTime() {
    //24 hours parking time should give 24 * parking fare per hour
    Date inTime = new Date(systemCurrentTimeMillis - (24 * 60 * 60 * 1000)); 
    Date outTime = new Date(systemCurrentTimeMillis);
    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

    ticket.setInTime(inTime);
    ticket.setOutTime(outTime);
    ticket.setParkingSpot(parkingSpot);
    fareCalculatorService.calculateFare(ticket);
    assertEquals((24 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
  } 

  @Test
  public void calculateFareCarWith5pcDiscount() {
    Date inTime = new Date(systemCurrentTimeMillis - (60 * 60 * 1000)); //Now - 1h
    Date outTime = new Date(systemCurrentTimeMillis); 
    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

    ticket.setInTime(inTime);
    ticket.setOutTime(outTime);
    ticket.setParkingSpot(parkingSpot);
    ticket.setDiscountPercentage(5);
    fareCalculatorService.calculateFare(ticket);
    assertEquals(Fare.CAR_RATE_PER_HOUR * 95 / 100, ticket.getPrice());
  } 

  @Test
  public void calculateFareBikeWith5pcDiscount() {
    Date inTime = new Date(systemCurrentTimeMillis - (60 * 60 * 1000)); //Now - 1h
    Date outTime = new Date(systemCurrentTimeMillis);
    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

    ticket.setInTime(inTime);
    ticket.setOutTime(outTime);
    ticket.setParkingSpot(parkingSpot);
    ticket.setDiscountPercentage(5);
    fareCalculatorService.calculateFare(ticket);
    assertEquals(Fare.BIKE_RATE_PER_HOUR * 95 / 100, ticket.getPrice());
  } 


} 
