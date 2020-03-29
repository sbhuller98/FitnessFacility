package ubc.cs304.team64.model;

import java.util.Objects;

public class Instructor {
  private final int iid;
  private final String name;
  private final double averageRating;
  private final double salary;

  public Instructor(int iid, String name, double averageRating, double salary) {
    this.iid = iid;
    this.name = name;
    this.averageRating = averageRating;
    this.salary = salary;
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
