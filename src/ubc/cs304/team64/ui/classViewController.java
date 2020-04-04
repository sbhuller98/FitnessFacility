package ubc.cs304.team64.ui;

import javafx.beans.value.ObservableValueBase;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.ResourceBundle;

import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import ubc.cs304.team64.model.Facility;
import ubc.cs304.team64.model.ClassInfo;
import ubc.cs304.team64.model.Instructor;
import ubc.cs304.team64.model.Member;


public class classViewController implements Initializable {



    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
    @FXML
    private TableView<ClassInfo> mainTable;
    @FXML
    private TableColumn<ClassInfo, String> titleCol;
    @FXML
    private TableColumn<ClassInfo, Timestamp> timeCol;
    @FXML
    private TableColumn<ClassInfo, String> DescriptionCol;
    @FXML
    private TableColumn<ClassInfo, String> capacityCol;
    @FXML
    private TableColumn<ClassInfo, String> instrucCol;

    public void startUp(Facility facility, Member member) {
        titleCol.setCellValueFactory(new ImmutablePropertyFactory<>(ClassInfo::getTitle));
        DescriptionCol.setCellValueFactory(new ImmutablePropertyFactory<>(ClassInfo::getDescription));
        capacityCol.setCellValueFactory(new ImmutablePropertyFactory<>(ClassInfo::getEnrollmentStatus));
        timeCol.setCellValueFactory(new ImmutablePropertyFactory<>(ClassInfo::getTime));
        instrucCol.setCellValueFactory(new ImmutablePropertyFactory<>(ClassInfo::getInstructorName));
        mainTable.setRowFactory((classInfoTableView -> new TableRow<ClassInfo>() {
          @Override
          public void updateItem(ClassInfo classInfo, boolean empty) {
            super.updateItem(classInfo, empty);
            boolean canTake = true;
            String style;
            if(empty || classInfo == null){
              style = "#229aeb";
            } else if(!member.canTakeClass(classInfo) || classInfo.getCurrentlyTaking() >= classInfo.getCapacity()){
              style = "grey";
              canTake = false;
            } else if (classInfo.IsOwnerTaking()){
              style = "green";
            } else {
              style = "#229aeb";
            }
            setStyle("-fx-background-color: "+style+";");
            setDisable(!canTake);
          }
        }));
        mainTable.getItems().setAll(SetUp(facility, member));
    }


    public Collection<ClassInfo> SetUp (Facility facility, Member member) {
        Collection<ClassInfo> allClasses = Main.connectionHandler.getClasses(facility, member);
        return allClasses;
        }



    static void setStage(Facility facility, Member member){
        FXMLLoaderWrapper<classViewController> loader = new FXMLLoaderWrapper<>("classView.fxml");
        loader.getController().startUp(facility, member);
        Main.updateStage(loader.getScene(), facility.getName());
    }
}


