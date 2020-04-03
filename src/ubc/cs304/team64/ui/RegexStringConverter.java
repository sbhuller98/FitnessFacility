package ubc.cs304.team64.ui;

import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.util.StringConverter;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RegexStringConverter extends StringConverter<String>{
  private Pattern pattern;
  private UnaryOperator<String> modification;

  public RegexStringConverter(String regexPattern){
    this(regexPattern, UnaryOperator.identity());
  }

  public RegexStringConverter(String regexPattern, UnaryOperator<String> modification){
    this.pattern = Pattern.compile(regexPattern);
    this.modification = modification;
  }

  @Override
  public String toString(String s) {
    return s == null ? "": s;
  }

  @Override
  public String fromString(String s) {
    String modified = modification.apply(s);
    if(pattern.matcher(modified).matches()){
      return modified;
    }
    throw new IllegalArgumentException();
  }

  public static String toTitleCase(String s){
    return Arrays.stream(s.split(" ")).map(s2 -> s2.substring(0,1).toUpperCase() + s2.substring(1).toLowerCase()).collect(Collectors.joining(" "));
  }
}
