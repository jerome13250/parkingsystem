package com.parkit.parkingsystem.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class DataBaseConfig {

  private static final Logger logger = LogManager.getLogger("DataBaseConfig");

  /**
   * Open a database Connection.
   *
   * @return Connection JDBC to a database
   * 
   * @see Connection
   * 
   */  
  public Connection getConnection() throws ClassNotFoundException, SQLException {
    logger.info("Create DB connection");
    Class.forName("com.mysql.cj.jdbc.Driver");
    return DriverManager.getConnection(
        "jdbc:mysql://localhost/prod?useUnicode=true&useJDBCCompliantTimezoneShift=true"
            + "&useLegacyDatetimeCode=false&serverTimezone=UTC", 
        "root", 
        "rootroot");
  } 


  /**
   * Close the Connection.
   *
   * @param con Connection to close.
   * 
   * @see Connection
   * 
   */  
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
   * Close the prepared statement.
   *
   * @param ps PreparedStatement to close.
   * 
   * @see PreparedStatement
   * 
   */
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
   * Close the ResultSet.
   *
   * @param rs ResultSet to close.
   * 
   * @see ResultSet
   * 
   */
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
