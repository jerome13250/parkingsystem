package com.parkit.parkingsystem.integration.service;

import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class DataBasePrepareService {

  DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();

  /**
   * Reset the tables to original state in the test database for integration test.
   * 
   * <ol>
   * <li>Set all parking spots to available in parking table</li>
   * <li>Delete all rows in ticket table</li>
   * </ol>
   * 
   */
  public void clearDataBaseEntries() {
    Connection connection = null;
    PreparedStatement ps1 = null;
    PreparedStatement ps2 = null;
    try {
      connection = dataBaseTestConfig.getConnection();

      //set parking entries to available
      ps1 = connection.prepareStatement("update parking set available = true");
      ps1.execute();

      //clear ticket entries;
      ps2 = connection.prepareStatement("truncate table ticket");
      ps2.execute();

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      dataBaseTestConfig.closePreparedStatement(ps1);
      dataBaseTestConfig.closePreparedStatement(ps2);
      dataBaseTestConfig.closeConnection(connection);
    } 
  } 


} 
