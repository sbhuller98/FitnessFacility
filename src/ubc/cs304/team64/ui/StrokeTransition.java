package ubc.cs304.team64.ui;


import javafx.animation.Transition;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.Collections;
import java.util.List;


public class StrokeTransition extends Transition {
  private Region parent;
  private Color start;
  private Color end;
  private BorderStroke base;

  public static StrokeTransition basicError(Region parent){
    return new StrokeTransition(parent, Duration.seconds(2), Color.RED, Color.TRANSPARENT);
  }

  public StrokeTransition(Region parent, Duration duration, Color start, Color end){
    setCycleDuration(duration);
    this.start = start;
    this.end = end;
    this.parent = parent;
    List<BorderStroke> strokes = parent.getBorder() == null ? Collections.emptyList() : parent.getBorder().getStrokes();
    if(strokes.size() != 0){
      base = strokes.get(0);
    } else {
      base = new BorderStroke(Color.TRANSPARENT, BorderStrokeStyle.SOLID, null, new BorderWidths(1));
    }
    parent.setBorder(new Border(base));
  }

  @Override
  protected void interpolate(double v) {
    Color curr = start.interpolate(end, v);
    BorderStroke borderStroke = new BorderStroke(curr, base.getBottomStyle(), base.getRadii(), base.getWidths());
    parent.borderProperty().setValue(new Border(borderStroke));
  }
}
