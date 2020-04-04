package ubc.cs304.team64.model;

import java.sql.Timestamp;
import java.util.Objects;

public class ClassInfo {
  private final Facility facility;
  private final int roomNumber;
  private final Timestamp time;
  private final String title;
  private final String description;
  private final String type;
  private final int iid;
  private final String instructorName;
  private final int capacity;
  private final int currentlyTaking;
  private final Member owner;
  private final boolean ownerIsTaking;

  ClassInfo(Facility facility, int roomNumber, Timestamp time, String title, String description, String type, int iid, String iName, int capacity, int currentlyTaking, Member owner, boolean ownerIsTaking) {
    this.facility = facility;
    this.roomNumber = roomNumber;
    this.time = time;
    this.title = title;
    this.description = description;
    this.type = type;
    this.iid = iid;
    this.instructorName = iName;
    this.capacity = capacity;
    this.currentlyTaking = currentlyTaking;
    this.owner = owner;
    this.ownerIsTaking = ownerIsTaking;
  }

  public Facility getFacility() {
    return facility;
  }

  public int getRoomNumber() {
    return roomNumber;
  }

  public Timestamp getTime() {
    return time;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public String getType() {
    return type;
  }

  public int getIid(){
    return iid;
  }

  public String getInstructorName() {return instructorName;}

  public int getCapacity() {
    return capacity;
  }

  public int getCurrentlyTaking() {
    return currentlyTaking;
  }

  public String getEnrollmentStatus(){
    return getCurrentlyTaking() + "/" +getCapacity();
  }

  public boolean IsOwnerTaking() {
    return ownerIsTaking;
  }

  Member getOwner() {return owner;}

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ClassInfo classInfo = (ClassInfo) o;
    return roomNumber == classInfo.roomNumber &&
        facility.equals(classInfo.facility) &&
        time.equals(classInfo.time);
  }

  @Override
  public int hashCode() {
    return Objects.hash(facility, roomNumber, time);
  }

  @Override
  public String toString() {
    return title + " on " + time.toString() + " in " + facility.getName() + " Room " + roomNumber;
  }
}
