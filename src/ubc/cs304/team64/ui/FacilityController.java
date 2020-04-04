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

public class FacilityController implements Initializable {

  @FXML
  private Label title;

  @FXML
  private ImageView logo;

  @FXML
  private ImageView image;

  @FXML
  private Button classes, instructors;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

  }

  public void setUp(Facility facility, Member member) {
    this.title.setText(facility.getName());
    logo.setImage(new Image(getClass().getResource("/facilities/logo" + facility.getFid() + ".jpg").toString()));
    image.setImage(new Image(getClass().getResource("/facilities/image" + facility.getFid() + ".jpg").toString()));
      classes.setOnAction(e -> classViewController.setStage(facility));
      instructors.setOnAction(e -> InstructorViewController.setStage(facility));
  }

  static void setStage(Facility facility, Member member){
    FXMLLoaderWrapper<FacilityController> loader = new FXMLLoaderWrapper<>("facility.fxml");
    loader.getController().setUp(facility, member);
    Main.updateStage(loader.getScene(), facility.getName());
  }
}
