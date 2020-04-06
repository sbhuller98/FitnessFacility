package ubc.cs304.team64.model;

public enum ClassColumn {
  INSTRUCTOR("name", "Instructor"),
  TITLE("title", "Title"),
  TYPE("type", "Type"),
  NONE("''", "None");

  public final String databaseName, uiName;
  ClassColumn(String databaseName, String uiName){
    this.databaseName = databaseName;
    this.uiName = uiName;
  }

  public String toString(){
    return uiName;
  }
}
