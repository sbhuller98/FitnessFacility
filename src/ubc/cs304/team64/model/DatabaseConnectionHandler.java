package ubc.cs304.team64.model;

import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

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
      Member retVal =  new Member(
          result.getInt("mid"),
          result.getString("address"),
          result.getString("phoneNumber"),
          result.getString("name"),
          result.getDate("birthDate"),
          result.getInt("driverLicenceNumber"),
          result.getString("sType"),
          result.getInt("cost")
      );
      ps.close();
      return retVal;
    } catch (SQLException e) {
      throw new Error(e);
    }
  }

  public Member createMember(String login, String password, String address, String phoneNumber, String name, LocalDate birthDate, int dln, String sType){
    try {
      int statusCost = getStatusCost(sType);
      if(phoneNumber.length() != 10 || !phoneNumber.matches("\\d*")){
        throw new InvalidParameterException("Phone number should be a 10 digit number");
      }

      String psString = "INSERT INTO member(login, password, address, phoneNumber, name, birthDate, driverLicenceNumber, sType) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
      PreparedStatement ps = connection.prepareStatement(psString, Statement.RETURN_GENERATED_KEYS);
      ps.setString(1, login);
      ps.setBytes(2, digest.digest(password.getBytes()));
      ps.setString(3, address);
      ps.setString(4, phoneNumber);
      ps.setString(5, name);
      ps.setDate(6, Date.valueOf(birthDate));
      ps.setInt(7, dln);
      ps.setString(8, sType);
      System.out.println(ps.executeUpdate());

      ResultSet autoKeys = ps.getGeneratedKeys();
      if ((!autoKeys.next())) throw new AssertionError();
      int mid = autoKeys.getInt(1);

      ps.close();
      connection.commit();
      return new Member(mid, address, phoneNumber, name, Date.valueOf(birthDate), dln, sType, statusCost);
    } catch (SQLIntegrityConstraintViolationException e){
      throw new IllegalArgumentException(e);
    }
    catch (SQLException e) {
      throw new Error(e);
    }
  }

  private int getStatusCost(String sType) throws SQLException{
    PreparedStatement getStatusCost = connection.prepareStatement("SELECT cost FROM status WHERE sType = ?");
    getStatusCost.setString(1, sType);
    ResultSet rs = getStatusCost.executeQuery();
    if(!rs.next()) {
      throw new IllegalSTypeException(sType);
    }
    int retVal = rs.getInt("cost");
    getStatusCost.close();
    return retVal;
  }

  public Collection<Facility> getFacilities() {
    Collection<Facility> retVal = new ArrayList<>();
    try {
      Statement s = connection.createStatement();
      ResultSet rs = s.executeQuery("SELECT * FROM facility");
      while(rs.next()) {
        Facility f = new Facility(
            rs.getInt("fid"),
            rs.getString("address"),
            rs.getString("name")
        );
        retVal.add(f);
      }
    } catch(SQLException e){
      throw new Error(e);
    }
    return retVal;
  }
}
