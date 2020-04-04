package ubc.cs304.team64.model;

import java.util.Date;
import java.util.Objects;
import java.util.Set;

public class Member {
  private final int mid;
  private final String address;
  private final String phoneNumber;
  private final String email;
  private final String name;
  private final Date birthDate;
  private final String statusType;
  private final double statusCost;
  private final Set<String> availableClassTypes;

  Member(int mid, String address, String phoneNumber, String email, String name, Date birthDate, String statusType, double statusCost, Set<String> availableClassTypes) {
    this.mid = mid;
    this.address = address;
    this.phoneNumber = "("+ phoneNumber.substring(0,3) +") "+ phoneNumber.substring(3,6)+"-"+phoneNumber.substring(6,10);
    this.email = email;
    this.name = name;
    this.birthDate = birthDate;
    this.statusType = statusType;
    this.statusCost = statusCost;
    this.availableClassTypes = availableClassTypes;
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

  public String getEmail(){
    return email;
  }

  public String getName() {
    return name;
  }

  public Date getBirthDate() {
    return birthDate;
  }

  public String getStatusType() {
    return statusType;
  }

  public double getStatusCost(){
    return statusCost;
  }

  public boolean canTakeClass(ClassInfo classInfo){
    return availableClassTypes.contains(classInfo.getType());
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
