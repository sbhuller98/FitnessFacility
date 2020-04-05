package ubc.cs304.team64.model;

import java.util.Objects;

public class Instructor {
  private final int iid;
  private final String name;
  private final double averageRating;
  private final double salary;
  private final String membersRating;
  private final Member member;

  public Instructor(int iid, String name, double averageRating, double salary, String membersRating, Member member) {
    this.iid = iid;
    this.name = name;
    this.averageRating = averageRating;
    this.salary = salary;
    this.membersRating = membersRating;
    this.member = member;
  }

  public int getIid() {
    return iid;
  }

  public String getName() {
    return name;
  }

  public double getAverageRating() {
    return averageRating;
  }

  public double getSalary() {
    return salary;
  }

  public String getMembersRating() {
    return membersRating;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Instructor that = (Instructor) o;
    return iid == that.iid;
  }

  @Override
  public int hashCode() {
    return Objects.hash(iid);
  }

  @Override
  public String toString() {
    return "Instructor #" + iid + " (" + name + ")";
  }
}
