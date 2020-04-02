package ubc.cs304.team64.model;

import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
      String sType = result.getString("sType");
      Member retVal =  new Member(
          result.getInt("mid"),
          result.getString("address"),
          result.getString("phoneNumber"),
          result.getString("name"),
          result.getDate("birthDate"),
          result.getInt("driverLicenceNumber"),
          sType,
          result.getInt("cost"),
          getClassTypesForStatus(sType)
      );
      ps.close();
      return retVal;
    } catch (SQLException e) {
      throw new Error(e);
    }
  }

  public Member createMember(String login, String password, String address, String phoneNumber, String name, LocalDate birthDate, int dln, String sType, Payment payment){
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

      PreparedStatement addPayment = connection.prepareStatement("INSERT INTO memberpayment(mid, pid) VALUES (?,?)");
      addPayment.setInt(1, mid);
      addPayment.setInt(2, payment.getPid());
      addPayment.executeUpdate();
      addPayment.close();
      connection.commit();
      return new Member(mid, address, phoneNumber, name, Date.valueOf(birthDate), dln, sType, statusCost, getClassTypesForStatus(sType));
    } catch (SQLIntegrityConstraintViolationException e){
      throw new IllegalArgumentException(e);
    }
    catch (SQLException e) {
      throw new Error(e);
    }
  }

  private Set<String> getClassTypesForStatus(String status){
    try{
      PreparedStatement ps = connection.prepareStatement("SELECT classType FROM letsyoutake WHERE sType = ?");
      ps.setString(1, status);
      ResultSet rs = ps.executeQuery();
      Set<String> retVal = new HashSet<>();
      while (rs.next()){
        retVal.add(rs.getString("classType"));
      }
      return retVal;
    } catch (SQLException e){
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

  public Payment createPayment(String frequency, long number, int csv, LocalDate expiryDate, String nameOnCard){
    try {
      PreparedStatement createCreditCard = connection.prepareStatement("INSERT INTO creditcard(num, expiryDate, csv, nameOnCard) VALUES (?,?,?,?)");
      createCreditCard.setLong(1, number);
      createCreditCard.setDate(2, Date.valueOf(expiryDate));
      createCreditCard.setInt(3, csv);
      createCreditCard.setString(4, nameOnCard);
      createCreditCard.executeUpdate();
      createCreditCard.close();

      PreparedStatement createPayment = connection.prepareStatement("INSERT INTO payment(frequency, creditCardNumber, accountNumber) VALUES (?, ?, NULL)", PreparedStatement.RETURN_GENERATED_KEYS);
      createPayment.setString(1, frequency);
      createPayment.setLong(2, number);
      createPayment.executeUpdate();
      ResultSet rs = createPayment.getGeneratedKeys();
      if ((!rs.next())) throw new AssertionError();
      int pid = rs.getInt(1);
      createPayment.close();
      connection.commit();
      return new CreditCard(pid, frequency, number, csv, Date.valueOf(expiryDate), nameOnCard);
    } catch (SQLIntegrityConstraintViolationException e){
      throw new IllegalArgumentException(e);
    } catch (SQLException e) {
      throw new Error(e);
    }
  }

  public Collection<Payment> getPayments(Member m){
    try {
      PreparedStatement base = connection.prepareStatement("SELECT * FROM payment NATURAL JOIN memberpayment WHERE mid = ?");
      base.setInt(1, m.getMid());
      ResultSet rs = base.executeQuery();
      Collection<Payment> retVal = new ArrayList<>();
      while (rs.next()){
        Payment result;
        int pid = rs.getInt("pid");
        String frequency = rs.getString("frequency");
        long creditCardNumber = rs.getLong("creditCardNumber");
        if(rs.wasNull()){
          int accountNumber = rs.getInt("accountNumber");
          result = getPAP(pid, frequency, accountNumber);
        } else {
          result = getCreditCard(pid, frequency, creditCardNumber);
        }
        retVal.add(result);
      }
      return retVal;
    } catch (SQLException e) {
      throw new Error(e);
    }
  }

  private CreditCard getCreditCard(int pid, String frequency, long creditCardNumber){
    try {
      PreparedStatement ps = connection.prepareStatement("SELECT * FROM creditcard WHERE num = ?");
      ps.setLong(1, creditCardNumber);
      ResultSet rs = ps.executeQuery();
      if ((!rs.next())) throw new AssertionError();
      CreditCard creditCard = new CreditCard(pid, frequency, creditCardNumber,
          rs.getInt("csv"),
          rs.getDate("expiryDate"),
          rs.getString("nameOnCard"));
      ps.close();
      return creditCard;
    } catch (SQLException e) {
      throw new Error(e);
    }
  }

  private PAPAccount getPAP(int pid, String frequency, int accountNumber){
    try {
      PreparedStatement ps = connection.prepareStatement("SELECT * FROM PAPAccount WHERE accountNumber = ?");
      ps.setLong(1, accountNumber);
      ResultSet rs = ps.executeQuery();
      if ((!rs.next())) throw new AssertionError();
      PAPAccount pap = new PAPAccount(pid, frequency, accountNumber,
          rs.getInt("bankNumber"),
          rs.getInt("transitNumber"));
      ps.close();
      return pap;
    } catch (SQLException e) {
      throw new Error(e);
    }
  }

  public Collection<Facility> getFacilities() {
    try {
      Collection<Facility> retVal = new ArrayList<>();
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
      return retVal;
    } catch(SQLException e){
      throw new Error(e);
    }
  }

  public Collection<ClassInfo> getClasses(Facility facility){
    try {
      PreparedStatement ps = connection.prepareStatement(
          "SELECT *," +
              "(SELECT COUNT(t.mid) FROM takes t " +
          "WHERE c.time = t.time AND c.rid = t.rid AND c.fid = t.fid) " +
          "as taking " +
          "FROM class c NATURAL JOIN classt " +
          "WHERE fid = ? AND time > CURRENT_TIMESTAMP()");
      ps.setInt(1, facility.getFid());
      Collection<ClassInfo> classes = new ArrayList<>();
      ResultSet rs = ps.executeQuery();
      while (rs.next()){
        ClassInfo classInfo = new ClassInfo(
            facility,
            rs.getInt("rid"),
            rs.getTimestamp("time"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getString("type"),
            rs.getInt("iid"),
            rs.getInt("capacity"),
            rs.getInt("taking")
        );
        classes.add(classInfo);
      }
      ps.close();
      return classes;
    } catch (SQLException e) {
      throw new Error(e);
    }
  }

  public Instructor getInstructor(ClassInfo classInfo){
    try {
      PreparedStatement ps = connection.prepareStatement("SELECT * FROM ratedinstructors i WHERE i.iid = ?");
      ps.setInt(1, classInfo.getIid());
      ResultSet rs = ps.executeQuery();
      if ((!rs.next())) throw new AssertionError();
      Instructor retVal = getInstructorFromRs(rs);
      ps.close();
      return retVal;
    } catch (SQLException e) {
      throw new Error(e);
    }
  }

  public Collection<Instructor> getInstructorsFromFacility(Facility facility){
    try {
      PreparedStatement ps = connection.prepareStatement("SELECT i.* FROM ratedinstructors i WHERE " +
          "i.iid IN (SELECT c.iid FROM class c WHERE c.time > CURRENT_TIMESTAMP AND c.iid = iid AND c.fid = ?)");
      ps.setInt(1, facility.getFid());
      ResultSet rs = ps.executeQuery();
      Collection<Instructor> retVal = new ArrayList<>();
      while (rs.next()){
        retVal.add(getInstructorFromRs(rs));
      }
      ps.close();
      return retVal;
    } catch (SQLException e) {
      throw new Error(e);
    }
  }

  private Instructor getInstructorFromRs(ResultSet rs) throws SQLException{
    return new Instructor(
        rs.getInt("iid"),
        rs.getString("name"),
        rs.getDouble("avgRating"),
        rs.getDouble("salary")
    );
  }
}
