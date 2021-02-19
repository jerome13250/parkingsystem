package com.parkit.parkingsystem.integration.config;

import com.parkit.parkingsystem.config.DataBaseConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DataBaseTestConfig extends DataBaseConfig {

  private static final Logger logger = LogManager.getLogger("DataBaseTestConfig");

  /**
   * Get a Connection to the test database for integration test.
   *
   * @return Connection to the test database.
   * 
   * @see Connection
   * 
   */ 
  @Override
  public Connection getConnection() throws ClassNotFoundException, SQLException {
    logger.info("Create DB connection");
    Class.forName("com.mysql.cj.jdbc.Driver");
    return DriverManager.getConnection(
        "jdbc:mysql://localhost/test?useUnicode=true&useJDBCCompliantTimezoneShift=true"
        + "&useLegacyDatetimeCode=false&serverTimezone=UTC", 
        "root", 
        "rootroot");
  } 

  /**
   * Close the Connection to the test database.
   *
   * @param con the Connection to close
   * 
   */
  @Override
  public void closeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
        logger.info("Closing DB connection");
      } catch (SQLException e) {
        logger.error("Error while closing connection", e);
      } 
    } 
  } 
  
  
  /**
   * Close a PreparedStatement in the test database.
   *
   * @param ps the PreparedStatement to close
   * 
   * @see PreparedStatement
   */ 
  @Override
  public void closePreparedStatement(PreparedStatement ps) {
    if (ps != null) {
      try {
        ps.close();
        logger.info("Closing Prepared Statement");
      } catch (SQLException e) {
        logger.error("Error while closing prepared statement", e);
      } 
    } 
  } 

  /**
   * Close a ResultSet.
   *
   * @param rs the ResultSet to close
   * 
   * @see ResultSet
   */ 
  @Override
  public void closeResultSet(ResultSet rs) {
    if (rs != null) {
      try {
        rs.close();
        logger.info("Closing Result Set");
      } catch (SQLException e) {
        logger.error("Error while closing result set", e);
      } 
    } 
  } 
} 
