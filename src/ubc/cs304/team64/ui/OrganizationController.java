package ubc.cs304.team64.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import ubc.cs304.team64.model.Instructor;
import ubc.cs304.team64.model.Member;
import ubc.cs304.team64.util.FXMLLoaderWrapper;


import java.net.URL;
import java.util.ResourceBundle;

public class OrganizationController implements Initializable {

    @FXML
    private Label description, besttrainer;

    @FXML Button back;




    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    public void setUp(Member member) {
        Instructor top = Main.connectionHandler.getBestInstructor();
        back.setOnAction(event -> FacilitiesController.setStage(member));
        String personalOutput = new StringBuilder()
                .append("Thank you for choosing GreaterVan Fitness. It has been our ,\n")
                .append("pleasure to serve the community  for 8 years now. Our company, \n")
                .append(" goal has been to provide a clean and safe environment where , \n")
                .append("you can focus on yourself.  We now have a number of facilities, \n")
                .append(" to support all of your fitness  needs. However, our organization, \n")
                .append(" is only as good as the people who represent us.  We are proud of, \n" )
                .append(" the individuals we hire, from our fitness team to our cleaning , \n")
                .append("team. Our amazing team of personal trainers is ready to help you, \n")
                .append(" get in shape.  Once again, thank you for giving us a chance. \n")
                .append("Enjoy your workout!, \n")
                .toString();
        description.setText(personalOutput);
        besttrainer.setText("Highest Rated Trainer: " + top.getName() + " has rating " + top.getAverageRating());
    }




    static void setStage(Member member){
        FXMLLoaderWrapper<OrganizationController> loader = new FXMLLoaderWrapper<>("organization.fxml");
        loader.getController().setUp(member);
        Main.updateStage(loader.getScene(), "GreaterVan Fitness");
    }



}