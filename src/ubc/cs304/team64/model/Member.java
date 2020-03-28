package ubc.cs304.team64.model;

import java.util.Date;
import java.util.Objects;

public class Member {
  private final int mid;
  private final String address;
  private final String phoneNumber;
  private final String name;
  private final Date birthDate;
  private final int driversLicenceNumber;
  private final String statusType;
  private final double statusCost;

  Member(int mid, String address, String phoneNumber, String name, Date birthDate, int driversLicenceNumber, String statusType, double statusCost) {
    this.mid = mid;
    this.address = address;
    this.phoneNumber = phoneNumber;
    this.name = name;
    this.birthDate = birthDate;
    this.driversLicenceNumber = driversLicenceNumber;
    this.statusType = statusType;
    this.statusCost = statusCost;
  }

  public int getMid() {
    return mid;
  }

  public String getAddress() {
    return address;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public String getName() {
    return name;
  }

  public Date getBirthDate() {
    return birthDate;
  }

  public int getDriversLicenceNumber() {
    return driversLicenceNumber;
  }

  public String getStatusType() {
    return statusType;
  }

  private double getStatusCost(){
    return statusCost;
  }

  @Override
  public String toString() {
    return "Member #" + mid + " (" + name + ")";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Member member = (Member) o;
    return mid == member.mid;
  }

  @Override
  public int hashCode() {
    return Objects.hash(mid);
  }
}
