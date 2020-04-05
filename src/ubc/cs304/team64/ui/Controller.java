package ubc.cs304.team64.ui;

import javafx.animation.Transition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;
import ubc.cs304.team64.model.InvalidLoginException;
import ubc.cs304.team64.model.Member;
import ubc.cs304.team64.util.FXMLLoaderWrapper;
import ubc.cs304.team64.util.StrokeTransition;

public class Controller implements Initializable {

    @FXML private TextField userNameField, passwordField;

    private Transition userNameTransition;
    private Transition passwordTransition;



    public void initialize (URL location, ResourceBundle resources) {
      userNameTransition = StrokeTransition.basicError(userNameField);
      passwordTransition = StrokeTransition.basicError(passwordField);
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
        passwordTransition.playFromStart();
        userNameTransition.playFromStart();
      }
    }
}
