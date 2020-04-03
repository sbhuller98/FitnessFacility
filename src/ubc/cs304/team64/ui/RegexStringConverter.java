package ubc.cs304.team64.ui;

import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.util.Arrays;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RegexStringConverter extends StringConverter<String>{
  private Pattern pattern;
  private UnaryOperator<String> modification;
  private StrokeTransition transition;

  public RegexStringConverter(String regexPattern, Region node){
    this(regexPattern, node, UnaryOperator.identity());
  }

  public RegexStringConverter(String regexPattern, Region node, UnaryOperator<String> modification){
    this.pattern = Pattern.compile(regexPattern);
    this.modification = modification;
    this.transition = new StrokeTransition(node, Duration.seconds(2), Color.RED, Color.TRANSPARENT);
    this.transition.jumpTo(Duration.seconds(2));
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
    transition.playFromStart();
    throw new IllegalArgumentException();
  }

  public static String toTitleCase(String s){
    return Arrays.stream(s.split(" ")).map(s2 -> s2.substring(0,1).toUpperCase() + s2.substring(1).toLowerCase()).collect(Collectors.joining(" "));
  }
}
