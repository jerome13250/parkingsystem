package com.parkit.parkingsystem.dao;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class TicketDAOTest {


  TicketDAO CUTticketDAO;
  boolean result;

  Date inTime;
  Date outTime;

  @Mock
  DataBaseConfig mockDataBaseConfig;
  @Mock
  Connection mockConn;
  @Mock
  PreparedStatement mockPreparedStmnt;
  @Mock
  ResultSet mockResultSet;


  @BeforeEach
  public void setUp() throws SQLException, ClassNotFoundException {

    CUTticketDAO = new TicketDAO(mockDataBaseConfig);

    when(mockDataBaseConfig.getConnection()).thenReturn(mockConn);
    //doNothing().when(mockConn).commit();

    //Generate inTime date for 05 Avril 2021. 10:10:00 AM
    Calendar cal = Calendar.getInstance();
    cal.set(2021, Calendar.APRIL, 5, 10, 10, 00); //Year, month, day , hours, min and sec
    inTime = cal.getTime();

    //Generate inTime date for 10 Avril 2021. 10:10:00 AM
    cal.set(2021, Calendar.APRIL, 10, 10, 10, 00); //Year, month, day , hours, min and sec
    outTime = cal.getTime();


  }


  @Test
  @DisplayName("Check that saveTicket correctly saves Ticket to database, case outTime is not null")
  void saveTicketTest_outTimeNotNull() throws SQLException {
    //GIVEN
    Ticket ticketToSave = new Ticket();
    ticketToSave.setParkingSpot(new ParkingSpot(111, ParkingType.BIKE, false));
    ticketToSave.setVehicleRegNumber("ABCDEF");
    ticketToSave.setPrice(99.99);
    ticketToSave.setInTime(inTime);
    ticketToSave.setOutTime(outTime);
    ticketToSave.setDiscountPercentage(33);

    when(mockConn.prepareStatement(DBConstants.SAVE_TICKET)).thenReturn(mockPreparedStmnt);
    doNothing().when(mockPreparedStmnt).setInt(1, 111); //PARKING_NUMBER
    doNothing().when(mockPreparedStmnt).setString(2, "ABCDEF"); //VEHICLE_REG_NUMBER
    doNothing().when(mockPreparedStmnt).setDouble(3, 99.99); //PRICE
    doNothing().when(mockPreparedStmnt).setTimestamp(4, new Timestamp(inTime.getTime())); //IN_TIME
    doNothing().when(mockPreparedStmnt).setTimestamp(5, new Timestamp(outTime.getTime())); //OUT_TIM
    doNothing().when(mockPreparedStmnt).setInt(6, 33); //DISCOUNT_PC
    when(mockPreparedStmnt.execute()).thenReturn(Boolean.TRUE);

    //WHEN
    result = CUTticketDAO.saveTicket(ticketToSave);

    //THEN
    verify(mockConn, times(1)).prepareStatement(any(String.class));
    verify(mockPreparedStmnt, times(2)).setInt(any(Integer.class), any(Integer.class));
    verify(mockPreparedStmnt, times(1)).setString(any(Integer.class), any(String.class));
    verify(mockPreparedStmnt, times(1)).setDouble(any(Integer.class), any(Double.class));
    verify(mockPreparedStmnt, times(2)).setTimestamp(any(Integer.class), any(Timestamp.class));
    verify(mockPreparedStmnt, times(1)).execute();
    assertTrue(result, "Mocked execute operation is successfull so result must be true");
  }

  @Test
  @DisplayName("Check that saveTicket correctly saves Ticket to database, case outTime is null")
  void saveTicketTest_outTimeNull() throws SQLException {
    //GIVEN
    Ticket ticketToSave = new Ticket();
    ticketToSave.setParkingSpot(new ParkingSpot(111, ParkingType.BIKE, false));
    ticketToSave.setVehicleRegNumber("ABCDEF");
    ticketToSave.setPrice(99.99);
    ticketToSave.setInTime(inTime);
    ticketToSave.setDiscountPercentage(33);

    when(mockConn.prepareStatement(DBConstants.SAVE_TICKET)).thenReturn(mockPreparedStmnt);
    doNothing().when(mockPreparedStmnt).setInt(1, 111); //PARKING_NUMBER
    doNothing().when(mockPreparedStmnt).setString(2, "ABCDEF"); //VEHICLE_REG_NUMBER
    doNothing().when(mockPreparedStmnt).setDouble(3, 99.99); //PRICE
    doNothing().when(mockPreparedStmnt).setTimestamp(4, new Timestamp(inTime.getTime())); //IN_TIME
    doNothing().when(mockPreparedStmnt).setTimestamp(5, null); //OUT_TIME
    doNothing().when(mockPreparedStmnt).setInt(6, 33);
    when(mockPreparedStmnt.execute()).thenReturn(Boolean.TRUE);

    //WHEN
    result = CUTticketDAO.saveTicket(ticketToSave);

    //THEN
    verify(mockConn, times(1)).prepareStatement(any(String.class));
    verify(mockPreparedStmnt, times(2)).setInt(any(Integer.class), any(Integer.class));
    verify(mockPreparedStmnt, times(1)).setString(any(Integer.class), any(String.class));
    verify(mockPreparedStmnt, times(1)).setDouble(any(Integer.class), any(Double.class));
    //If timestmamp == null, the method is not called, so only 1 call here for inTime:
    verify(mockPreparedStmnt, times(1)).setTimestamp(any(Integer.class), any(Timestamp.class)); 
    verify(mockPreparedStmnt, times(1)).execute();
    assertTrue(result, "Mocked execute operation is successfull so result must be true");
  }

  @Test
  @DisplayName("Check that saveTicket returns false when an exception occurs")
  void saveTicketTest_Exception() throws SQLException {
    //GIVEN
    Ticket ticketToSave = new Ticket();
    when(mockConn.prepareStatement(DBConstants.SAVE_TICKET)).thenThrow(SQLException.class);

    //WHEN
    result = CUTticketDAO.saveTicket(ticketToSave);

    //THEN
    assertTrue(!result, "Mocked prepareStatement throws SQLException so saveTicket result must be false");
  }


  @Test
  @DisplayName("Check that getTicket correctly creates a Ticket from database data")
  void getTicketTest() throws SQLException {
    //GIVEN
    when(mockConn.prepareStatement(DBConstants.GET_TICKET)).thenReturn(mockPreparedStmnt);
    doNothing().when(mockPreparedStmnt).setString(1, "ABCDEF"); //REGNUMBER
    when(mockPreparedStmnt.executeQuery()).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(Boolean.TRUE, Boolean.FALSE); //RS has 1 line result
    when(mockResultSet.getInt(1)).thenReturn(12345); //PARKING_NUMBER
    when(mockResultSet.getInt(2)).thenReturn(888); //ID
    when(mockResultSet.getDouble(3)).thenReturn(999.99d); //PRICE
    when(mockResultSet.getTimestamp(4)).thenReturn(new Timestamp(inTime.getTime())); //IN_TIME
    when(mockResultSet.getTimestamp(5)).thenReturn(new Timestamp(outTime.getTime())); //OUT_TIME
    when(mockResultSet.getInt(6)).thenReturn(33); //DISCOUNT_PC
    when(mockResultSet.getString(7)).thenReturn("BIKE"); //TYPE

    //WHEN
    Ticket resultTicket = CUTticketDAO.getTicket("ABCDEF");

    //THEN
    //Ticket:
    assertEquals("ABCDEF", resultTicket.getVehicleRegNumber(), "RegNumber must equals mock value");
    assertEquals(888, resultTicket.getId(), "id must equal mock value");
    assertEquals(999.99, resultTicket.getPrice(), "price must equal mock value");
    assertEquals(inTime, resultTicket.getInTime(), "inTime must equal mock value");
    assertEquals(outTime, resultTicket.getOutTime(), "outTime must equal mock value");
    assertEquals(33, resultTicket.getDiscountPercentage(), "discount must equal mock value");
    //ParkingSpot:
    assertEquals(12345, resultTicket.getParkingSpot().getId(), 
        "parkingSpot id must equal mock value");
    assertEquals(ParkingType.BIKE, resultTicket.getParkingSpot().getParkingType(),
        "parkingType must equal mock value");
  }

  @Test
  @DisplayName("Check that getTicket returns null Ticket when regnumber is not found")
  void getTicketTest_RegNumberNotFoundInDatabase() throws SQLException {
    //GIVEN
    when(mockConn.prepareStatement(DBConstants.GET_TICKET)).thenReturn(mockPreparedStmnt);
    doNothing().when(mockPreparedStmnt).setString(1, "ABCDEF"); //REGNUMBER
    when(mockPreparedStmnt.executeQuery()).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(Boolean.FALSE); //RS has no line result

    //WHEN
    Ticket resultTicket = CUTticketDAO.getTicket("ABCDEF");

    //THEN
    assertNull(resultTicket, "must return null Ticket when regnumber is not found");
  }

  @Test
  @DisplayName("Check that getTicket returns null Ticket when SQLException occurs")
  void getTicketTest_SQLException() throws SQLException{
    //GIVEN
    when(mockConn.prepareStatement(DBConstants.GET_TICKET)).thenThrow(SQLException.class);

    //WHEN
    Ticket resultTicket = CUTticketDAO.getTicket("ABCDEF");

    //THEN
    assertNull(resultTicket, "must return null Ticket when SQLException occurs");
  }
  
  
  @Test
  @DisplayName("Check that updateTicket returns true and correctly updates Ticket to database")
  void updateTicketTest() throws SQLException {
    //GIVEN
    Ticket ticketToUpdate = new Ticket();
    ticketToUpdate.setPrice(99.99);
    ticketToUpdate.setOutTime(outTime);
    ticketToUpdate.setId(666);

    when(mockConn.prepareStatement(DBConstants.UPDATE_TICKET)).thenReturn(mockPreparedStmnt);
    doNothing().when(mockPreparedStmnt).setDouble(1, 99.99); //PRICE
    doNothing().when(mockPreparedStmnt).setTimestamp(2, new Timestamp(outTime.getTime())); //OUT_TIM
    doNothing().when(mockPreparedStmnt).setInt(3, 666); //id
    when(mockPreparedStmnt.execute()).thenReturn(Boolean.TRUE);

    //WHEN
    result = CUTticketDAO.updateTicket(ticketToUpdate);

    //THEN
    verify(mockConn, times(1)).prepareStatement(any(String.class));
    verify(mockPreparedStmnt, times(1)).setInt(any(Integer.class), any(Integer.class));
    verify(mockPreparedStmnt, times(1)).setDouble(any(Integer.class), any(Double.class));
    verify(mockPreparedStmnt, times(1)).setTimestamp(any(Integer.class), any(Timestamp.class));
    verify(mockPreparedStmnt, times(1)).execute();
    assertTrue(result, "Mocked execute operation is successfull so result must be true");
  }
  
  @Test
  @DisplayName("Check that updateTicket returns false when SQLException occurs")
  void updateTicketTest_SQLException() throws SQLException {
    //GIVEN
    Ticket ticketToUpdate = new Ticket();
    when(mockConn.prepareStatement(DBConstants.UPDATE_TICKET)).thenThrow(SQLException.class);

    //WHEN
    result = CUTticketDAO.updateTicket(ticketToUpdate);

    //THEN
    assertTrue(!result, 
        "Mocked prepareStatement throws SQLException so saveTicket result must be false");
  }
  

}
