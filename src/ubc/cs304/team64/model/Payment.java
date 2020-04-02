package ubc.cs304.team64.model;

public abstract class Payment {
  private int pid;
  private String frequency;

  public Payment(int pid, String frequency) {
    this.pid = pid;
    this.frequency = frequency;
  }

  public int getPid() {
    return pid;
  }

  public String getFrequency() {
    return frequency;
  }

  @Override
  public String toString() {
    return "Payment #" + pid;
  }
}
