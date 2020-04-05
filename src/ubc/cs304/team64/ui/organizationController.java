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




    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    public void setUp(Member member) {
        back.setOnAction(e -> FacilitiesController.setStage(member));

    }

    static void setStage(Member member){
        FXMLLoaderWrapper<organizationController> loader = new FXMLLoaderWrapper<>("organization.fxml");
        loader.getController().setUp(member);
        Main.updateStage(loader.getScene();
    }


}