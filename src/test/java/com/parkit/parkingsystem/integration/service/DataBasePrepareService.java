package com.parkit.parkingsystem.integration.service;

import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import java.sql.Connection;

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
    try {
      connection = dataBaseTestConfig.getConnection();

      //set parking entries to available
      connection.prepareStatement("update parking set available = true").execute();

      //clear ticket entries;
      connection.prepareStatement("truncate table ticket").execute();

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      dataBaseTestConfig.closeConnection(connection);
    } 
  } 


} 
