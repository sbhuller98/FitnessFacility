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

public class facilityMapController implements Initializable {

    Member member1;
    Facility facility1;

    @FXML
    private ImageView image;

    @FXML Button back;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        back.setOnAction(e -> FacilityController.setStage(facility1, member1));
    }

    public void setUp(Facility facility, Member member) {
        facility1 = facility;
        member1 = member;
        image.setImage(new Image(getClass().getResource("/facilities/fmap" + facility.getFid() + ".jpg").toString()));

    }

    static void setStage(Facility facility, Member member){

        FXMLLoaderWrapper<facilityMapController> loader = new FXMLLoaderWrapper<>("facilityMap.fxml");
        loader.getController().setUp(facility, member);
        Main.updateStage(loader.getScene(), facility.getName());
    }
}
