package ubc.cs304.team64.model;

import java.util.Date;

public class CreditCard extends Payment{
  private long number;
  private int csv;
  private Date expiryDate;
  private String name;

  public CreditCard(int pid, String frequency, long number, int csv, Date expiryDate, String name) {
    super(pid, frequency);
    this.number = number;
    this.csv = csv;
    this.expiryDate = expiryDate;
  }

  public long getNumber() {
    return number;
  }

  public int getCsv() {
    return csv;
  }

  public Date getExpiryDate() {
    return expiryDate;
  }

  public String getName(){
    return name;
  }
}
