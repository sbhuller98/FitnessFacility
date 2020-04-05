package ubc.cs304.team64.ui;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;

import java.util.function.Function;

public class FunctionalBinding<T, R> extends ObjectBinding<R> {
  private ObservableValue<T> value;
  private Function<T, R> function;
  public FunctionalBinding(ObservableValue<T> value, Function<T, R> function){
    super();
    super.bind(value);
    this.value = value;
    this.function = function;
  }

  protected R computeValue(){
    return function.apply(value.getValue());
  }
}
