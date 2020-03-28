package ubc.cs304.team64.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class DatabaseConnectionHandler {
  private Connection connection;
  private MessageDigest digest;

  public DatabaseConnectionHandler(){
    try {
      // Load the JDBC driver
      // Note that the path could change for new drivers
      DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
      connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/Project", "root", "CPSC304g64");
      connection.setAutoCommit(false);
      digest = MessageDigest.getInstance("MD5");

      System.out.println("\nConnected to Database!");
    } catch (SQLException | NoSuchAlgorithmException e) {
      throw new Error(e);
    }
  }

  public Member getMember(String login, String password) throws InvalidLoginException {
    try {
      PreparedStatement ps = connection.prepareStatement("SELECT * FROM member NATURAL JOIN status WHERE login = ? AND password = ?");
      ps.setString(1, login);
      ps.setBytes(2, digest.digest(password.getBytes()));
      ResultSet result = ps.executeQuery();
      if(!result.next()){
        throw new InvalidLoginException();
      }
      return new Member(
          result.getInt("mid"),
          result.getString("address"),
          result.getString("phoneNumber"),
          result.getString("name"),
          result.getDate("birthDate"),
          result.getInt("driverLicenceNumber"),
          result.getString("sType"),
          result.getInt("cost")
      );
    } catch (SQLException e) {
      throw new Error(e);
    }
  }

  public static void main(String[] args) throws Throwable {
    DatabaseConnectionHandler handler = new DatabaseConnectionHandler();
    Member ian = handler.getMember("imiller", "pa$$word");
    System.out.println(ian);
  }
}
