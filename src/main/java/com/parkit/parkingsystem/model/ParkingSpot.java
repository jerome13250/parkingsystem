package com.parkit.parkingsystem.model;

import com.parkit.parkingsystem.constants.ParkingType;

public class ParkingSpot {
  private int number;
  private ParkingType parkingType;
  private boolean isAvailable;
  
  /**
   * Constructor for the ParkingSpot class.
   *
   * @param number the parking spot number, unique identifier of the parking spot.
   * @param parkingType An enum ParkingType, type of the parking spot (car, bike, etc...)
   * @param isAvailable boolean true if parking spot is available, false if not.
   *
   * @see ParkingType
   * 
   */
  public ParkingSpot(int number, ParkingType parkingType, boolean isAvailable) {
    this.number = number;
    this.parkingType = parkingType;
    this.isAvailable = isAvailable;
  } 

  public int getId() {
    return number;
  } 

  public void setId(int number) {
    this.number = number;
  } 

  public ParkingType getParkingType() {
    return parkingType;
  } 

  public void setParkingType(ParkingType parkingType) {
    this.parkingType = parkingType;
  } 

  public boolean isAvailable() {
    return isAvailable;
  } 

  public void setAvailable(boolean available) {
    isAvailable = available;
  } 

  @Override
  public boolean equals(Object o) {
    if (this == o) { 
      return true; 
    }
    if (o == null || getClass() != o.getClass()) {
      return false; 
    } 
    ParkingSpot that = (ParkingSpot) o;
    return number == that.number;
  } 

  @Override
  public int hashCode() {
    return number;
  } 
} 
