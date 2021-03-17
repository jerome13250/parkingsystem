package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ParkingSpotDAO {
  private static final Logger logger = LogManager.getLogger("ParkingSpotDAO");

  public DataBaseConfig dataBaseConfig = new DataBaseConfig();

  
  /**
   * Get the next parking slot available according to the parking type required.
   *
   * @param parkingType The type of parking spot, must be an enum ParkingType.
   *
   * @return Integer that is the parking spot number available. 0 if no parking spot available.
   * 
   * @see ParkingType
   * 
   */  
  public int getNextAvailableSlot(ParkingType parkingType) {
    Connection con = null;
    int result = -1;
    try {
      con = dataBaseConfig.getConnection();
      PreparedStatement ps = con.prepareStatement(DBConstants.GET_NEXT_PARKING_SPOT);
      ps.setString(1, parkingType.toString());
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {  //min() in sql returns an empty line with null even if no parking available 
        result = rs.getInt(1); // if value is SQL NULL, the value returned is 0
      } 
      dataBaseConfig.closeResultSet(rs);
      dataBaseConfig.closePreparedStatement(ps);
    } catch (Exception ex) {
      logger.error("Error fetching next available slot", ex);
    } finally {
      dataBaseConfig.closeConnection(con);
    } 
    return result;
  } 
  
  /**
   * Update a specific parking spot availability in database.
   *
   * @param parkingSpot The parking spot object to update in database.
   *
   * @return Integer that is the parking spot number available.
   * 
   * @see ParkingTypeExtended
   * 
   */   
  public boolean updateParking(ParkingSpot parkingSpot) {
    //update the availability fo that parking slot
    Connection con = null;
    try {
      con = dataBaseConfig.getConnection();
      PreparedStatement ps = con.prepareStatement(DBConstants.UPDATE_PARKING_SPOT);
      ps.setBoolean(1, parkingSpot.isAvailable());
      ps.setInt(2, parkingSpot.getId());
      int updateRowCount = ps.executeUpdate();
      dataBaseConfig.closePreparedStatement(ps);
      return (updateRowCount == 1);
    } catch (Exception ex) {
      logger.error("Error updating parking info", ex);
      return false;
    } finally {
      dataBaseConfig.closeConnection(con);
    } 
  } 

} 
