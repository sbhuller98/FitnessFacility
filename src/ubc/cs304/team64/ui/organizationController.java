package ubc.cs304.team64.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ubc.cs304.team64.model.Facility;
import ubc.cs304.team64.model.Member;
import ubc.cs304.team64.util.FXMLLoaderWrapper;


import java.net.URL;
import java.util.ResourceBundle;

public class organizationController implements Initializable {

    @FXML
    private Label text;

    @FXML Button back;




    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }






    static void setStage(Member member){

        FacilitiesController.setStage(member);
    }



}