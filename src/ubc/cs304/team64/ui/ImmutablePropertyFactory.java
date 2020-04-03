package ubc.cs304.team64.ui;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class ImmutablePropertyFactory<S, T> implements Callback<TableColumn.CellDataFeatures<S, T>, ObservableValue<T>> {
  private Callback<S, T> callback;
  public ImmutablePropertyFactory(Callback<S, T> callback){
    this.callback = callback;
  }
  @Override
  public ObservableValue<T> call(TableColumn.CellDataFeatures<S, T> stCellDataFeatures) {
    T result = this.callback.call(stCellDataFeatures.getValue());
    return new ObservableValue<T>() {
      @Override
      public void addListener(ChangeListener<? super T> changeListener) {}

      @Override
      public void removeListener(ChangeListener<? super T> changeListener) {}

      @Override
      public T getValue() {
        return result;
      }

      @Override
      public void addListener(InvalidationListener invalidationListener) {}

      @Override
      public void removeListener(InvalidationListener invalidationListener) {}
    };
    }
}
