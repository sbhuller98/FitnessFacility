package ubc.cs304.team64.ui;

import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ubc.cs304.team64.model.Facility;
import ubc.cs304.team64.model.Member;

import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;

public class FacilitiesController implements Initializable {
  @FXML private ImageView logo1, logo2, logo3, logo4, logo5;
  private ImageView[] logos;

  @FXML private Button button1, button2, button3, button4, button5;
  private Button[] buttons;

  @FXML private Button back;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    logos = new ImageView[]{logo1, logo2, logo3, logo4, logo5};
    buttons = new Button[]{button1, button2, button3, button4, button5};
    back.setOnAction(e -> Main.updateStage(new FXMLLoaderWrapper<>("Login.fxml").getScene(), "Login"));
  }

  public void setUp(Member m){
    Collection<Facility> facilities = Main.connectionHandler.getFacilities();
    int i = 0;
    for(Facility f : facilities){
      if(i >= 5){
        break;
      }
      logos[i].setImage(new Image(getClass().getResource("/facilities/logo" + f.getFid() + ".jpg").toString()));
      buttons[i].setText(f.getName());
      buttons[i].setOnAction(e -> FacilityController.setStage(f, m));
      i++;
    }
  }

  static void setStage(Member member){
    FXMLLoaderWrapper<FacilitiesController> loader = new FXMLLoaderWrapper<>("facilities.fxml");
    loader.getController().setUp(member);
    Main.updateStage(loader.getScene(), "Facilities");
  }
}
