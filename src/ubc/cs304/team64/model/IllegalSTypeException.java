package ubc.cs304.team64.model;

public class IllegalSTypeException extends IllegalArgumentException{
  IllegalSTypeException(String sType){
    super(sType + " is not a valid sType");
  }
}
