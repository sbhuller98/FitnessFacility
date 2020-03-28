package ubc.cs304.team64.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnectionHandler {
  private Connection connection;

  public DatabaseConnectionHandler(){
    try {
      // Load the JDBC driver
      // Note that the path could change for new drivers
      DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
      connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/Project", "root", "CPSC304g64");
      connection.setAutoCommit(false);

      System.out.println("\nConnected to Database!");
    } catch (SQLException e) {
      System.err.println(e);
    }
  }
}
