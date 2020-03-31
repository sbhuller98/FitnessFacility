package ubc.cs304.team64.ui;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;

import java.io.IOException;

public class FXMLLoaderWrapper<E extends Initializable> {
  private FXMLLoader loader;
  private Scene scene;

  public FXMLLoaderWrapper(String fxml){
    loader = new FXMLLoader(FXMLLoaderWrapper.class.getResource(fxml));
    try {
      scene = new Scene(loader.load());
    } catch (IOException e) {
      throw new Error(e);
    }
  }

  public Scene getScene(){
    return scene;
  }

  public E getController(){
    return loader.getController();
  }
}
