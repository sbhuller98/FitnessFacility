package ubc.cs304.team64.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ubc.cs304.team64.model.Facility;


import java.net.URL;
import java.util.ResourceBundle;

public class FacilityController implements Initializable {

  @FXML
  private Label title;

  @FXML
  private ImageView logo;

  @FXML
  private ImageView image;

  private Facility facility;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

  }

  public void setFacility(Facility facility) {
    this.facility = facility;
    this.title.setText(facility.getName());
    logo.setImage(new Image(getClass().getResource("/facilities/logo" + facility.getFid() + ".jpg").toString()));
    image.setImage(new Image(getClass().getResource("/facilities/image" + facility.getFid() + ".jpg").toString()));
  }

  static void setStage(Facility facility){
    FXMLLoaderWrapper<FacilityController> loader = new FXMLLoaderWrapper<>("facility.fxml");
    loader.getController().setFacility(facility);
    Main.updateStage(loader.getScene(), facility.getName());
  }
}
