package com.parkit.parkingsystem.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
public class ParkingSpotDAOTest {

  ParkingSpotDAO CUTparkingSpotDAO;
  boolean updateParkingSuccess = false;

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

    CUTparkingSpotDAO = new ParkingSpotDAO(mockDataBaseConfig);
    updateParkingSuccess = false;
    when(mockDataBaseConfig.getConnection()).thenReturn(mockConn);

  }

  @Test
  @DisplayName("Check that getNextAvailableSlot correctly returns the parking spot number")
  void getNextAvailableSlot() throws SQLException {
    //GIVEN
    when(mockConn.prepareStatement(DBConstants.GET_NEXT_PARKING_SPOT))
        .thenReturn(mockPreparedStmnt);
    doNothing().when(mockPreparedStmnt).setString(1, "BIKE"); //REGNUMBER
    when(mockPreparedStmnt.executeQuery()).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(Boolean.TRUE, Boolean.FALSE); //RS has 1 line result
    when(mockResultSet.getInt(1)).thenReturn(12345); //PARKING_NUMBER

    //WHEN
    int nextAvailableParkingSpotId = CUTparkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE);

    //THEN
    assertEquals(12345, nextAvailableParkingSpotId, "returned ParkingSpotId must equal mock value");
  }
  
  
  @Test
  @DisplayName("Check that getNextAvailableSlot returns -1 when ResultSet is empty")
  void getNextAvailableSlot_ResultSetEmpty() throws SQLException {
    //GIVEN
    when(mockConn.prepareStatement(DBConstants.GET_NEXT_PARKING_SPOT))
        .thenReturn(mockPreparedStmnt);
    doNothing().when(mockPreparedStmnt).setString(1, "BIKE"); //REGNUMBER
    when(mockPreparedStmnt.executeQuery()).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(Boolean.FALSE); //RS has no line result

    //WHEN
    int nextAvailableParkingSpotId = CUTparkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE);

    //THEN
    //Ticket:
    assertEquals(-1, nextAvailableParkingSpotId, 
        "returned ParkingSpotId must be -1 if ResultSet is empty");
  }
  
  
  @Test
  @DisplayName("Check that getNextAvailableSlot returns -1 when SQLException occurs")
  void getNextAvailableSlot_SQLException() throws SQLException {
    //GIVEN
    when(mockConn.prepareStatement(DBConstants.GET_NEXT_PARKING_SPOT))
        .thenThrow(SQLException.class);

    //WHEN
    int nextAvailableParkingSpotId = CUTparkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE);

    //THEN
    //Ticket:
    assertEquals(-1, nextAvailableParkingSpotId, 
        "returned ParkingSpotId must be -1 if SQLException occurs");
  }
  
  @Test
  @DisplayName("Check that updateParking returns true and correctly updates ParkingSpot to db")
  void updateParkingTest() throws SQLException {
    //GIVEN
    ParkingSpot parkingSpotToUpdate = new ParkingSpot(333, ParkingType.BIKE, true);

    when(mockConn.prepareStatement(DBConstants.UPDATE_PARKING_SPOT)).thenReturn(mockPreparedStmnt);
    doNothing().when(mockPreparedStmnt).setBoolean(1, true); //available
    doNothing().when(mockPreparedStmnt).setInt(2, 333); //PARKING_NUMBER
    when(mockPreparedStmnt.executeUpdate()).thenReturn(1);

    //WHEN
    updateParkingSuccess = CUTparkingSpotDAO.updateParking(parkingSpotToUpdate);

    //THEN
    verify(mockConn, times(1)).prepareStatement(any(String.class));
    verify(mockPreparedStmnt, times(1)).setInt(any(Integer.class), any(Integer.class));
    verify(mockPreparedStmnt, times(1)).setBoolean(any(Integer.class), any(Boolean.class));
    verify(mockPreparedStmnt, times(1)).executeUpdate();
    assertTrue(updateParkingSuccess, "Mocked operation is successfull so result must be true");
  }
  
  
  @Test
  @DisplayName("Check that updateParking returns false if number of updated row > 1")
  void updateParkingTest_NumberUpdatedRowInconsistent() throws SQLException {
    //GIVEN
    ParkingSpot parkingSpotToUpdate = new ParkingSpot(333, ParkingType.BIKE, true);

    when(mockConn.prepareStatement(DBConstants.UPDATE_PARKING_SPOT)).thenReturn(mockPreparedStmnt);
    doNothing().when(mockPreparedStmnt).setBoolean(1, true); //available
    doNothing().when(mockPreparedStmnt).setInt(2, 333); //PARKING_NUMBER
    when(mockPreparedStmnt.executeUpdate()).thenReturn(2); //number of updated row > 1

    //WHEN
    updateParkingSuccess = CUTparkingSpotDAO.updateParking(parkingSpotToUpdate);

    //THEN
    verify(mockConn, times(1)).prepareStatement(any(String.class));
    verify(mockPreparedStmnt, times(1)).setInt(any(Integer.class), any(Integer.class));
    verify(mockPreparedStmnt, times(1)).setBoolean(any(Integer.class), any(Boolean.class));
    verify(mockPreparedStmnt, times(1)).executeUpdate();
    assertTrue(!updateParkingSuccess, 
        "returned value must be false if number of updated row > 1");
  }
  
  @Test
  @DisplayName("Check that updateParking returns false if SQLException occurs")
  void updateParkingTest_SQLException() throws SQLException {
    //GIVEN
    ParkingSpot parkingSpotToUpdate = new ParkingSpot(333, ParkingType.BIKE, true);
    when(mockConn.prepareStatement(DBConstants.UPDATE_PARKING_SPOT)).thenThrow(SQLException.class);

    //WHEN
    updateParkingSuccess = CUTparkingSpotDAO.updateParking(parkingSpotToUpdate);

    //THEN
    assertTrue(!updateParkingSuccess, 
        "returned value must be false if SQLException occurs");
  }
  
  

}
