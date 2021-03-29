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


class FareCalculatorServiceTest {

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
  void calculateFareOutTimeNullThrowsIllegalArgumentException() {
    //GIVEN
    Date inTime = new Date(systemCurrentTimeMillis);
    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
    ticket.setInTime(inTime);
    ticket.setParkingSpot(parkingSpot);

    //THEN
    assertThrows(
        IllegalArgumentException.class,
        () -> fareCalculatorService.calculateFare(ticket),
        "outTime can't be null to calculate Fare"
    );
  } 

  @Test
  void calculateFareCar() {
    //GIVEN
    Date inTime = new Date(systemCurrentTimeMillis - (60 * 60 * 1000)); //Now - 1h
    Date outTime = new Date(systemCurrentTimeMillis); 
    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
    ticket.setInTime(inTime);
    ticket.setOutTime(outTime);
    ticket.setParkingSpot(parkingSpot);
    
    //WHEN
    double price = fareCalculatorService.calculateFare(ticket);
    
    //THEN
    assertEquals(
        Fare.CAR_RATE_PER_HOUR, 
        price,
        "For 1 hour car parking, fare must be equal to Fare.CAR_RATE_PER_HOUR");
  } 

  @Test
  void calculateFareBike() {
    //GIVEN
    Date inTime = new Date(systemCurrentTimeMillis - (60 * 60 * 1000)); //Now - 1h
    Date outTime = new Date(systemCurrentTimeMillis);
    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
    ticket.setInTime(inTime);
    ticket.setOutTime(outTime);
    ticket.setParkingSpot(parkingSpot);
    
    //WHEN
    double price = fareCalculatorService.calculateFare(ticket);
    
    //THEN
    assertEquals(Fare.BIKE_RATE_PER_HOUR,
        price,
        "For 1 hour bike parking, fare must be equal to Fare.BIKE_RATE_PER_HOUR");
  } 

  @Test
  void calculateFareParkingTypeNull() {
    //GIVEN
    Date inTime = new Date(systemCurrentTimeMillis - (60 * 60 * 1000)); //Now - 1h
    Date outTime = new Date(systemCurrentTimeMillis);
    ParkingSpot parkingSpot = new ParkingSpot(1, null, false);
    ticket.setInTime(inTime);
    ticket.setOutTime(outTime);
    ticket.setParkingSpot(parkingSpot);
    
    //THEN
    assertThrows(
        NullPointerException.class,
        () -> fareCalculatorService.calculateFare(ticket),
        "Null ParkingType throws NullPointerException");
  } 

  @Test
  void calculateFareInTimeAfterOutTime() {
    //GIVEN
    Date inTime = new Date(systemCurrentTimeMillis + (60 * 60 * 1000)); //Now + 1h
    Date outTime = new Date(systemCurrentTimeMillis);
    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
    ticket.setInTime(inTime);
    ticket.setOutTime(outTime);
    ticket.setParkingSpot(parkingSpot);
    
    //THEN
    assertThrows(
        IllegalArgumentException.class,
        () -> fareCalculatorService.calculateFare(ticket),
        "InTime > OutTime must throw IllegalArgumentException");
  } 

  @Test
  void calculateFareBikeWithLessThanOneHourParkingTime() {
    //45 minutes parking time should give 3/4th parking fare:
    //GIVEN
    Date inTime = new Date(systemCurrentTimeMillis - (45 * 60 * 1000));
    Date outTime = new Date(systemCurrentTimeMillis);
    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
    ticket.setInTime(inTime);
    ticket.setOutTime(outTime);
    ticket.setParkingSpot(parkingSpot);
    
    //WHEN
    double price = fareCalculatorService.calculateFare(ticket);
    
    //THEN
    assertEquals(
        (0.75 * Fare.BIKE_RATE_PER_HOUR),
        price,
        "3/4 hour fare for bike must be 3/4 of Fare.BIKE_RATE_PER_HOUR");
  } 

  @Test
  void calculateFareBikeWithLessThan30MinutesParkingTime() {
    //30 minutes or less parking time should be free
    //GIVEN
    Date inTime = new Date(systemCurrentTimeMillis - (25 * 60 * 1000)); 
    Date outTime = new Date(systemCurrentTimeMillis);
    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
    ticket.setInTime(inTime);
    ticket.setOutTime(outTime);
    ticket.setParkingSpot(parkingSpot);
    
    //WHEN
    double price = fareCalculatorService.calculateFare(ticket);
    
    //THEN
    assertEquals(
        0,
        price,
        "Bike fare for less than 30 minutes must be 0");
  } 

  @Test
  void calculateFareCarWithLessThanOneHourParkingTime() {
    //45 minutes parking time should give 3/4th parking fare:
    //GIVEN
    Date inTime = new Date(systemCurrentTimeMillis - (45 * 60 * 1000));
    Date outTime = new Date(systemCurrentTimeMillis);
    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
    ticket.setInTime(inTime);
    ticket.setOutTime(outTime);
    ticket.setParkingSpot(parkingSpot);
    
    //WHEN
    double price = fareCalculatorService.calculateFare(ticket);
    
    //THEN
    assertEquals(
        (0.75 * Fare.CAR_RATE_PER_HOUR),
        price,
        "3/4 hour fare for car must be 3/4 of Fare.CAR_RATE_PER_HOUR");
  } 

  @Test
  void calculateFareCarWithLessThan30MinutesParkingTime() {
    //30 minutes or less parking time should be free
    //GIVEN
    Date inTime = new Date(systemCurrentTimeMillis - (25 * 60 * 1000)); 
    Date outTime = new Date(systemCurrentTimeMillis);
    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
    ticket.setInTime(inTime);
    ticket.setOutTime(outTime);
    ticket.setParkingSpot(parkingSpot);
    
    //WHEN
    double price = fareCalculatorService.calculateFare(ticket);
    
    //THEN
    assertEquals(
        0,
        ticket.getPrice(),
        "Car fare for less than 30 minutes must be 0");
  } 


  @Test
  void calculateFareCarWithMoreThan24hParkingTime() {
    //24 hours parking time should give 24 * parking fare per hour
    //GIVEN
    Date inTime = new Date(systemCurrentTimeMillis - (30 * 60 * 60 * 1000)); 
    Date outTime = new Date(systemCurrentTimeMillis);
    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
    ticket.setInTime(inTime);
    ticket.setOutTime(outTime);
    ticket.setParkingSpot(parkingSpot);

    //WHEN
    double price = fareCalculatorService.calculateFare(ticket);
    
    //THEN
    assertEquals(
        (30 * Fare.CAR_RATE_PER_HOUR),
        price,
        "Car fare for 30 hours must be ( 30 * Fare.CAR_RATE_PER_HOUR )");
  } 
  
  @Test
  void calculateFareBikeWithMoreThan24hParkingTime() {
    //24 hours parking time should give 24 * parking fare per hour
    //GIVEN
    Date inTime = new Date(systemCurrentTimeMillis - (30 * 60 * 60 * 1000)); 
    Date outTime = new Date(systemCurrentTimeMillis);
    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

    ticket.setInTime(inTime);
    ticket.setOutTime(outTime);
    ticket.setParkingSpot(parkingSpot);

    //WHEN
    double price = fareCalculatorService.calculateFare(ticket);
    
    //THEN
    assertEquals(
        (30 * Fare.BIKE_RATE_PER_HOUR),
        price,
        "Bike fare for 30 hours must be ( 30 * Bike.CAR_RATE_PER_HOUR )");
  } 

  @Test
  void calculateFareCarWith5pcDiscount() {
    //GIVEN
    Date inTime = new Date(systemCurrentTimeMillis - (60 * 60 * 1000)); //Now - 1h
    Date outTime = new Date(systemCurrentTimeMillis); 
    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
    ticket.setInTime(inTime);
    ticket.setOutTime(outTime);
    ticket.setParkingSpot(parkingSpot);
    ticket.setDiscountPercentage(5);

    //WHEN
    double price = fareCalculatorService.calculateFare(ticket);
    
    //THEN
    assertEquals(
        Fare.CAR_RATE_PER_HOUR * 95 / 100,
        price,
        "Car fare for 1 hour and 5% discount must be 95% of Fare.CAR_RATE_PER_HOUR");
  } 

  @Test
  void calculateFareBikeWith5pcDiscount() {
    //GIVEN
    Date inTime = new Date(systemCurrentTimeMillis - (60 * 60 * 1000)); //Now - 1h
    Date outTime = new Date(systemCurrentTimeMillis);
    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
    ticket.setInTime(inTime);
    ticket.setOutTime(outTime);
    ticket.setParkingSpot(parkingSpot);
    ticket.setDiscountPercentage(5);

    //WHEN
    double price = fareCalculatorService.calculateFare(ticket);
    
    //THEN
    assertEquals(
        Fare.BIKE_RATE_PER_HOUR * 95 / 100,
        price,
        "Bike fare for 1 hour and 5% discount must be 95% of Fare.Bike_RATE_PER_HOUR");
  } 


} 
