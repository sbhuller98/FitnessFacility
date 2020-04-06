package ubc.cs304.team64.model;

import javax.lang.model.type.NullType;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

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
          result.getString("email"),
          result.getString("name"),
          result.getDate("birthDate"),
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

  public Collection<String> getStatuses(){
    try{
      Statement s = connection.createStatement();
      ResultSet rs = s.executeQuery("SELECT sType FROM status");
      Collection<String> retVal = new ArrayList<>();
      while (rs.next()){
        retVal.add(rs.getString("sType"));
      }
      return retVal;
    } catch (SQLException e){
      throw new Error(e);
    }
  }

  public Member updatePersonal(Member original, String name, String address, String email, String phone) {
      Statement stmt = null;
      try {
          if (phone.length() != 10 || !phone.matches("\\d*")) {
              throw new InvalidParameterException("Phone number should be a 10 digit number");
          }

          PreparedStatement ps = connection.prepareStatement("SELECT * FROM MEMBER WHERE mid = ?", ResultSet.TYPE_SCROLL_SENSITIVE,
                  ResultSet.CONCUR_UPDATABLE);
          ps.setInt(1, original.getMid());
          ResultSet result = ps.executeQuery();
          if (!result.next()) {
              throw new AssertionError();
          }
          result.updateString("address", address);
          result.updateString("name", name);
          result.updateString("email", email);
          result.updateString("phoneNumber", phone);
          result.updateRow();
          ps.close();
          connection.commit();
          return new Member(original.getMid(), address, phone, email, name, original.getBirthDate(), original.getStatusType(), original.getStatusCost(), original.getAvailableClassTypes());
      } catch (SQLIntegrityConstraintViolationException e) {
          throw new Error(e);
      } catch (SQLException e) {
          throw new IllegalArgumentException(e);
      }
  }

  public void updatePayment(Member member, Payment payment){
    try {
      PreparedStatement ps = connection.prepareStatement("INSERT INTO memberpayment(mid, pid) VALUES (?, ?)");
      ps.setInt(1, member.getMid());
      ps.setInt(2, payment.getPid());
      ps.executeUpdate();
      ps.close();
      connection.commit();
    } catch (SQLException e) {
      throw new Error(e);
    }
  }

    public void updatepass(Member original, String password) {
        Statement stmt;
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM MEMBER WHERE mid = ?", ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            ps.setInt(1, original.getMid());
            ResultSet result = ps.executeQuery();
            if (!result.next()) {
                throw new InvalidLoginException();
            }

            result.updateBytes("password", digest.digest(password.getBytes()));
            result.updateRow();
            ps.close();
            connection.commit();
        } catch (SQLException e) {
            throw new Error(e);
        } catch (InvalidLoginException e) {
            throw new IllegalArgumentException(e);
        }
    }
  
  public Member createMember(String login, String password, String address, String phoneNumber, String email, String name, LocalDate birthDate, String sType, Payment payment){
    try {
      int statusCost = getStatusCost(sType);
      if(phoneNumber.length() != 10 || !phoneNumber.matches("\\d*")){
        throw new InvalidParameterException("Phone number should be a 10 digit number");
      }

      String psString = "INSERT INTO member(login, password, address, phoneNumber, email, name, birthDate, sType) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
      PreparedStatement ps = connection.prepareStatement(psString, Statement.RETURN_GENERATED_KEYS);
      ps.setString(1, login);
      ps.setBytes(2, digest.digest(password.getBytes()));
      ps.setString(3, address);
      ps.setString(4, phoneNumber);
      ps.setString(5, email);
      ps.setString(6, name);
      ps.setDate(7, Date.valueOf(birthDate));
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
      return new Member(mid, address, phoneNumber, email, name, Date.valueOf(birthDate), sType, statusCost, getClassTypesForStatus(sType));
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
            rs.getString("name"),
            rs.getString("description")
        );
        retVal.add(f);
      }
      return retVal;
    } catch(SQLException e){
      throw new Error(e);
    }
  }

  public List<ClassInfo> getClasses(Facility facility, Member member, ClassColumn colName, String colVal){
    if(colName == ClassColumn.NONE){
      colVal = "";
    }
    try {
      PreparedStatement ps = connection.prepareStatement(
          "SELECT *, COUNT(t.mid) as taking, " +
              "        ? IN (SELECT mid FROM takes t2 WHERE c.time = t2.time AND c.rid = t2.rid AND c.fid = t2.fid) as isMemberTaking " +
              "    FROM takes t NATURAL RIGHT OUTER JOIN class c NATURAL JOIN classt ct NATURAL JOIN instructor i " +
              "    WHERE fid = ? AND time > CURRENT_TIMESTAMP AND "+ colName.databaseName + " LIKE ? " +
              "    GROUP BY c.time, c.rid, c.fid");
      ps.setInt(1, member == null ? -1 : member.getMid());
      ps.setInt(2, facility.getFid());
      ps.setString(3, colVal);
      return getClassesBase(facility, member, ps, true);
    } catch (SQLException e) {
      throw new Error(e);
    }
  }

  public Collection<ClassInfo> getRegisteredClasses(Facility facility, Member member){
    try {
      PreparedStatement ps = connection.prepareStatement(
     "SELECT *, COUNT(t.mid) as taking " +
          "FROM  takes t NATURAL RIGHT OUTER JOIN class c NATURAL JOIN classt NATURAL JOIN instructor i " +
          "WHERE fid = ? AND time > CURRENT_TIMESTAMP AND ? IN (SELECT mid FROM takes t2 WHERE c.time = t2.time AND c.rid = t2.rid AND c.fid = t2.fid) " +
          "GROUP BY c.time, c.title, c.fid");
      ps.setInt(1, facility.getFid());
      ps.setInt(2, member.getMid());
      return getClassesBase(facility, member, ps, false);
    } catch (SQLException e) {
      throw new Error(e);
    }
  }

  private List<ClassInfo> getClassesBase(Facility facility, Member member, PreparedStatement ps, boolean hasExtraField) throws SQLException {
    List<ClassInfo> classes = new ArrayList<>();
    ResultSet rs = ps.executeQuery();
    while (rs.next()){
      ClassInfo classInfo = new ClassInfo (
          facility,
          rs.getInt("rid"),
          rs.getTimestamp("time"),
          rs.getString("title"),
          rs.getString("description"),
          rs.getString("type"),
          rs.getInt("iid"),
          rs.getString("name"),
          rs.getInt("capacity"),
          rs.getInt("taking"),
          member,
          !hasExtraField || rs.getBoolean("isMemberTaking")
      );
      classes.add(classInfo);
    }
    ps.close();
    return classes;
  }

  public Collection<Instructor> getInstructorsFromFacility(Facility facility, Member member){
    try {
      PreparedStatement ps = connection.prepareStatement("SELECT i.*, r.rating FROM ratedinstructors i NATURAL LEFT OUTER JOIN " +
          "(SELECT * FROM rates WHERE mid = ?) r " +
          "WHERE i.iid IN (SELECT c.iid FROM class c WHERE c.time > CURRENT_TIMESTAMP AND c.iid = iid AND c.fid = ?)");
      ps.setInt(1, member.getMid());
      ps.setInt(2, facility.getFid());
      ResultSet rs = ps.executeQuery();
      Collection<Instructor> retVal = new ArrayList<>();
      while (rs.next()){
        String memberRating = String.valueOf(rs.getInt("rating"));
        if(rs.wasNull()){
          memberRating = "";
        }
        Instructor i = new Instructor(
            rs.getInt("iid"),
            rs.getString("name"),
            rs.getDouble("avgRating"),
            rs.getDouble("salary"),
            memberRating,
            member
        );
        retVal.add(i);
      }
      ps.close();
      return retVal;
    } catch (SQLException e) {
      throw new Error(e);
    }
  }

  public void rateInstructor(Member member, Instructor instructor, int rating){
    try {
      PreparedStatement ps = connection.prepareStatement("SELECT * FROM rates WHERE (mid = ? AND iid = ?)", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
      ps.setInt(1, member.getMid());
      ps.setInt(2, instructor.getIid());
      ResultSet rs = ps.executeQuery();
      if(rs.next()){
        rs.updateInt("rating", rating);
        rs.updateRow();
      } else {
        PreparedStatement ps2 = connection.prepareStatement("INSERT INTO rates(mid, iid, rating) VALUES (?, ? ,?)");
        ps2.setInt(1, member.getMid());
        ps2.setInt(2, instructor.getIid());
        ps2.setInt(3, rating);
        ps2.executeUpdate();
        ps2.close();
      }
      ps.close();
      connection.commit();
    } catch (SQLException e) {
      throw new Error(e);
    }

  }
  public Instructor bestInstructor(){
    try {
      Statement s = connection.createStatement();
      ResultSet rs = s.executeQuery(
          "SELECT * FROM ratedinstructors i WHERE i.avgRating = " +
              "(SELECT MAX(i2.avgRating) FROM ratedinstructors i2)"
      );
      if(!rs.next()) throw new AssertionError();
      return new Instructor(
          rs.getInt("iid"),
          rs.getString("name"),
          rs.getDouble("avgRating"),
          rs.getDouble("salary"),
          null, null
      );
    } catch (SQLException e) {
      throw new Error(e);
    }
  }

  public void registerMemberForClass(ClassInfo classInfo){
    alterRegistration(classInfo, "INSERT INTO takes(mid, time, rid, fid) VALUES (?, ?, ?, ?)");
  }

  public void deregisterMemberForClass(ClassInfo classInfo){
    alterRegistration(classInfo, "DELETE FROM takes WHERE mid = ? AND time = ? AND rid = ? AND fid = ?");
  }

  private void alterRegistration(ClassInfo classInfo, String psString){
    try {
      PreparedStatement ps = connection.prepareStatement(psString);
      ps.setInt(1, classInfo.getOwner().getMid());
      ps.setTimestamp(2, classInfo.getTime());
      ps.setInt(3, classInfo.getRoomNumber());
      ps.setInt(4, classInfo.getFacility().getFid());
      ps.executeUpdate();
      connection.commit();
      ps.close();
    } catch (SQLIntegrityConstraintViolationException e){
      throw new IllegalArgumentException(e);
    } catch (SQLException e) {
      throw new Error(e);
    }
  }
}
