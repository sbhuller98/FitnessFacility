package ubc.cs304.team64.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ubc.cs304.team64.model.Facility;
import ubc.cs304.team64.model.Member;
import ubc.cs304.team64.util.FXMLLoaderWrapper;


import java.net.URL;
import java.util.ResourceBundle;

public class FacilityMapController implements Initializable {

    @FXML
    private ImageView image;

    @FXML Button back;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    public void setUp(Facility facility, Member member) {
        image.setImage(new Image(getClass().getResource("/facilities/fmap" + facility.getFid() + ".jpg").toString()));
        back.setOnAction(e -> FacilityController.setStage(facility, member));
    }

    static void setStage(Facility facility, Member member){

        FXMLLoaderWrapper<FacilityMapController> loader = new FXMLLoaderWrapper<>("facilityMap.fxml");
        loader.getController().setUp(facility, member);
        Main.updateStage(loader.getScene(), facility.getName());
    }
}
