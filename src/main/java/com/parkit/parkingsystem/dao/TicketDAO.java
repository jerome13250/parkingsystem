package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TicketDAO {

  private static final Logger logger = LogManager.getLogger("TicketDAO");

  public DataBaseConfig dataBaseConfig = new DataBaseConfig();

  /**
   * Save a Ticket object to database.
   *
   * @param ticket The Ticket to save.
   *
   * @return true if the save successes, false otherwise.
   * 
   * @see Ticket
   * 
   */  
  public boolean saveTicket(Ticket ticket) {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = dataBaseConfig.getConnection();
      ps = con.prepareStatement(DBConstants.SAVE_TICKET);
      //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
      //ps.setInt(1, ticket.getId());
      ps.setInt(1, ticket.getParkingSpot().getId());
      ps.setString(2, ticket.getVehicleRegNumber());
      ps.setDouble(3, ticket.getPrice());
      ps.setTimestamp(4, new Timestamp(ticket.getInTime().getTime()));
      ps.setTimestamp(5, 
          (ticket.getOutTime() == null) ? null : (new Timestamp(ticket.getOutTime().getTime())));
      ps.setInt(6, ticket.getDiscountPercentage());
      return ps.execute();
    } catch (Exception ex) {
      logger.error("Error fetching next available slot", ex);
    } finally {
      dataBaseConfig.closePreparedStatement(ps);
      dataBaseConfig.closeConnection(con);
    } 
    return false;
  } 

  /**
   * Get a Ticket object from the database with required vehicle registration number.
   *
   * @param vehicleRegNumber the required vehicle registration number.
   *
   * @return Ticket object.
   * 
   * @see Ticket
   * 
   */
  public Ticket getTicket(String vehicleRegNumber) {
    Connection con = null;
    Ticket ticket = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      con = dataBaseConfig.getConnection();
      //TODO: la requete parait fausse "order by t.IN_TIME  " donc si on a plusieurs entrees on sortira toujours la plus vieille ????
      ps = con.prepareStatement(DBConstants.GET_TICKET); 
      //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
      ps.setString(1, vehicleRegNumber);
      rs = ps.executeQuery();
      if (rs.next()) {
        ticket = new Ticket();
        ParkingSpot parkingSpot = new ParkingSpot(
            rs.getInt(1), ParkingType.valueOf(rs.getString(7)), false);
        ticket.setParkingSpot(parkingSpot);
        ticket.setId(rs.getInt(2));
        ticket.setVehicleRegNumber(vehicleRegNumber);
        ticket.setPrice(rs.getDouble(3));
        ticket.setInTime(rs.getTimestamp(4));
        ticket.setOutTime(rs.getTimestamp(5));
        ticket.setDiscountPercentage(rs.getInt(6));
      } 
    } catch (Exception ex) {
      logger.error("Error fetching next available slot", ex);
    } finally {
      dataBaseConfig.closeResultSet(rs);
      dataBaseConfig.closePreparedStatement(ps);
      dataBaseConfig.closeConnection(con);
    } 
    return ticket;

  } 


  /**
   * Update a Ticket object in the database.
   *
   * @param ticket the ticket to update.
   *
   * @return boolean that indicates success or failure of the update.
   * 
   * @see Ticket
   * 
   */
  public boolean updateTicket(Ticket ticket) {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = dataBaseConfig.getConnection();
      ps = con.prepareStatement(DBConstants.UPDATE_TICKET);
      ps.setDouble(1, ticket.getPrice());
      ps.setTimestamp(2, new Timestamp(ticket.getOutTime().getTime()));
      ps.setInt(3, ticket.getId());
      ps.execute();
      return true;
    } catch (Exception ex) {
      logger.error("Error saving ticket info", ex);
    } finally {
      dataBaseConfig.closePreparedStatement(ps);
      dataBaseConfig.closeConnection(con);
    } 
    return false;
  } 

} 



