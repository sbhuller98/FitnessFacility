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

import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.*;

public class Controller implements Initializable {




    @FXML
    private Button buttonSignUp;





    public void initialize (URL location, ResourceBundle resources) {


    }

    public void launchSignUp(javafx.event.ActionEvent actionEvent) throws Exception {
        Stage stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
        Parent signUp = FXMLLoader.load(getClass().getResource("signUp.fxml"));

        Scene signUpScene = new Scene(signUp);

        stage.setTitle("SignUp");
        stage.setScene(signUpScene);
        stage.show();
    }
}
