package ubc.cs304.team64.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javafx.event.ActionEvent;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.*;
import ubc.cs304.team64.model.InvalidLoginException;
import ubc.cs304.team64.model.Member;

public class Controller implements Initializable {




    @FXML
    private Button buttonSignUp;

    @FXML private TextField userNameField, passwordField;



    public void initialize (URL location, ResourceBundle resources) {


    }

    public void launchSignUp() throws Exception {
        Main.updateStage(new FXMLLoaderWrapper<>("signUp.fxml").getScene(), "SignUp");
    }

    public void login(){
      String userName = userNameField.getText();
      String password = passwordField.getText();
      try{
        Member m = Main.connectionHandler.getMember(userName, password);
        FacilitiesController.setStage(m);
      } catch (InvalidLoginException e) {
        // TODO create error message
      }
    }
}
