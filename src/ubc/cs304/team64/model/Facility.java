package ubc.cs304.team64.model;

import java.util.Objects;

public class Facility{
  private final int fid;
  private final String address;
  private final String name;
  private final String description;

  Facility(int fid, String address, String name, String description) {
    this.fid = fid;
    this.address = address;
    this.name = name;
    this.description = description;
  }

  public int getFid() {
    return fid;
  }

  public String getAddress() {
    return address;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Facility facility = (Facility) o;
    return fid == facility.fid;
  }

  @Override
  public int hashCode() {
    return Objects.hash(fid);
  }

  @Override
  public String toString(){
    return "Facility #" + fid + " (" + name + ")";
  }
}

