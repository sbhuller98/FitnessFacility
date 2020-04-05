package ubc.cs304.team64.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import ubc.cs304.team64.model.ClassInfo;
import ubc.cs304.team64.model.Facility;
import ubc.cs304.team64.model.Member;
import ubc.cs304.team64.util.FXMLLoaderWrapper;
import ubc.cs304.team64.util.ImmutablePropertyFactory;


import java.net.URL;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.ResourceBundle;

public class MyAccountController implements Initializable {

    @FXML
    private TableView<ClassInfo> table;
    @FXML
    private TableColumn<ClassInfo, Timestamp> time;
    @FXML
    private TableColumn<ClassInfo, Integer> room;
    @FXML
    private TableColumn<ClassInfo, String> title;

    @FXML
    private ListView<String> memAvailable;

    @FXML
    private Label memType, memCost, personalDetails;



    @FXML Button back;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void setUp(Facility facility, Member member) {
        back.setOnAction(e -> FacilityController.setStage(facility, member));
        title.setCellValueFactory(new ImmutablePropertyFactory<>(ClassInfo::getTitle));
        room.setCellValueFactory(new ImmutablePropertyFactory<>(ClassInfo::getRoomNumber));
        time.setCellValueFactory(new ImmutablePropertyFactory<>(ClassInfo::getTime));
        table.getItems().setAll(getClassInfo(facility, member));

        ObservableList<String> items = FXCollections.observableArrayList(member.getAvailableClassTypes());
        memAvailable.setItems(items);

        memType.setText("Your membership type is: " + member.getStatusType());
        memCost.setText("Your monthly cost is: $" + member.getStatusCost() + "0");
        String personalOutput = new StringBuilder()
                .append("Name: " + member.getName() + "\n")
                .append("Member ID: " + member.getMid() + "\n")
                .append("Address: " + member.getAddress() + "\n")
                .append("Email: " + member.getEmail() + "\n")
                .append("Date of Birth: " + member.getBirthDate() + "\n")
                .toString();
        personalDetails.setText(personalOutput);

    }

    public Collection<ClassInfo> getClassInfo (Facility facility, Member member) {
        Collection<ClassInfo> allClasses = Main.connectionHandler.getClasses(facility, member);
        return allClasses;
    }


    static void setStage(Facility facility, Member member){

        FXMLLoaderWrapper<MyAccountController> loader = new FXMLLoaderWrapper<>("myAccount.fxml");
        loader.getController().setUp(facility, member);
        Main.updateStage(loader.getScene(), facility.getName());
    }
}
