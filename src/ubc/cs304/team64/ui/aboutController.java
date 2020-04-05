package ubc.cs304.team64.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ubc.cs304.team64.model.Facility;
import ubc.cs304.team64.model.Member;


import java.net.URL;
import java.util.ResourceBundle;

public class aboutController implements Initializable {
    @FXML
    private Label description, address;

    @FXML
    private ImageView image, logo;

    @FXML Button back;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    public void setUp(Facility facility, Member member) {
        back.setOnAction(e -> FacilityController.setStage(facility, member));
        description.setWrapText(true);
        address.setWrapText(true);
        description.setText(facility.getDescription());
        address.setText("Address: " + facility.getAddress());
        image.setImage(new Image(getClass().getResource("/facilities/image" + facility.getFid() + ".jpg").toString()));
        logo.setImage(new Image(getClass().getResource("/facilities/logo" + facility.getFid() + ".jpg").toString()));



    }

    static void setStage(Facility facility, Member member){

        FXMLLoaderWrapper<AboutController> loader = new FXMLLoaderWrapper<>("about.fxml");
        loader.getController().setUp(facility, member);
        Main.updateStage(loader.getScene(), facility.getName());
    }
}
