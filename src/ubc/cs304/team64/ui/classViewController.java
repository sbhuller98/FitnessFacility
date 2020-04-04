package ubc.cs304.team64.ui;

import javafx.animation.PauseTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.Timer;

import javafx.scene.control.TableView;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.util.Duration;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import ubc.cs304.team64.model.Facility;
import ubc.cs304.team64.model.ClassInfo;
import ubc.cs304.team64.model.Member;


public class classViewController implements Initializable {

    Facility facility1;
    Member member1;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        back.setOnAction(e -> FacilityController.setStage(facility1, member1));
    }
    @FXML
    private TableView<ClassInfo> mainTable;
    @FXML
    private TableColumn<ClassInfo, String> titleCol;
    @FXML
    private TableColumn<ClassInfo, Timestamp> timeCol;
    @FXML
    private TableColumn<ClassInfo, Integer> roomCol;
    @FXML
    private TableColumn<ClassInfo, String> descriptionCol;
    @FXML
    private TableColumn<ClassInfo, String> capacityCol;
    @FXML
    private TableColumn<ClassInfo, String> instructorCol;
    @FXML
    private Button register;

    public void startUp(Facility facility, Member member) {
        titleCol.setCellValueFactory(new ImmutablePropertyFactory<>(ClassInfo::getTitle));
        roomCol.setCellValueFactory(new ImmutablePropertyFactory<>(ClassInfo::getRoomNumber));
        descriptionCol.setCellValueFactory(new ImmutablePropertyFactory<>(ClassInfo::getDescription));
        capacityCol.setCellValueFactory(new ImmutablePropertyFactory<>(ClassInfo::getEnrollmentStatus));
        timeCol.setCellValueFactory(new ImmutablePropertyFactory<>(ClassInfo::getTime));
        instructorCol.setCellValueFactory(new ImmutablePropertyFactory<>(ClassInfo::getInstructorName));
        mainTable.setRowFactory((classInfoTableView -> new TableRow<ClassInfo>() {
          @Override
          public void updateItem(ClassInfo classInfo, boolean empty) {
            super.updateItem(classInfo, empty);
            boolean canTake = false;
            String style;
            if(empty || classInfo == null){
              style = "#229aeb";
            } else if(!member.canTakeClass(classInfo) || classInfo.getCurrentlyTaking() >= classInfo.getCapacity()){
              style = "grey";
            } else if (classInfo.IsOwnerTaking()){
              style = "green";
            } else {
              style = "#229aeb";
              canTake = true;
            }
            setStyle("-fx-background-color: "+style+";");
            setDisable(!canTake);
          }
        }));
        mainTable.getItems().setAll(SetUp(facility, member));

        PauseTransition pt = new PauseTransition(Duration.millis(2));
        pt.setOnFinished((e) -> mainTable.getSelectionModel().clearSelection());
        mainTable.getSelectionModel().selectedIndexProperty().addListener((observableValue, old, next) -> {
          if(next.intValue() == -1){
            return;
          }
          int dir = old.intValue() < next.intValue() ? 1 : -1;
          int n = mainTable.getItems().size();
          for (int i = next.intValue(); i >= 0 && i < n; i+=dir) {
            ClassInfo c = mainTable.getItems().get(i);
            if(!c.IsOwnerTaking() && member.canTakeClass(c) && c.getCurrentlyTaking() < c.getCapacity()){
              if(i == next.intValue()){
                return;
              } else {
                final int finalI = i;
                pt.setOnFinished(e -> mainTable.getSelectionModel().clearAndSelect(finalI));
                pt.play();
                return;
              }
            }
          }
          pt.setOnFinished(e -> mainTable.getSelectionModel().clearSelection());
          pt.play();
        });
        register.disableProperty().bind(mainTable.getSelectionModel().selectedItemProperty().isNull());
        register.setOnAction(t -> resister());
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

    public void resister() {
        ClassInfo selected = mainTable.getSelectionModel().getSelectedItem();
        try {
          Main.connectionHandler.registerMemberForClass(selected);
        } catch (IllegalArgumentException e){
          e.printStackTrace();
          // TODO handle
        }
        classViewController.setStage(selected.getFacility(), member);
    }
}


