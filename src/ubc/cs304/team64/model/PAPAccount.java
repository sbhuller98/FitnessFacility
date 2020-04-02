package ubc.cs304.team64.model;

public class PAPAccount extends Payment{
  private int accountNumber;
  private int bankNumber;
  private int transitNumber;

  public PAPAccount(int pid, String frequency, int accountNumber, int bankNumber, int transitNumber) {
    super(pid, frequency);
    this.accountNumber = accountNumber;
    this.bankNumber = bankNumber;
    this.transitNumber = transitNumber;
  }

  public int getAccountNumber() {
    return accountNumber;
  }

  public int getBankNumber() {
    return bankNumber;
  }

  public int getTransitNumber() {
    return transitNumber;
  }
}
