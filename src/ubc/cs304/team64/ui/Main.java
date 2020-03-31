package ubc.cs304.team64.ui;

import ubc.cs304.team64.*;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ubc.cs304.team64.model.DatabaseConnectionHandler;
import ubc.cs304.team64.model.Facility;

import java.util.Collection;
import java.util.Iterator;


public class Main extends Application {
    private static Stage stage;
    public static DatabaseConnectionHandler connectionHandler;

    public static void main(String[] args){
        System.out.println("Program is running");

        launch(args);
    }


    @Override
    public void start(Stage stage) throws Exception {
        Main.stage = stage;
        Main.connectionHandler = new DatabaseConnectionHandler();
        updateStage(new FXMLLoaderWrapper<>("Login.fxml").getScene(), "Login");
    }

    public static void updateStage(Scene scene, String title){
      stage.setScene(scene);
      stage.setTitle(title);
      stage.show();
    }

}
