package ubc.cs304.team64.ui;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.ResourceBundle;

import javafx.scene.control.TableView;
import javafx.scene.control.Button;
import javafx.util.Duration;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import ubc.cs304.team64.model.Facility;
import ubc.cs304.team64.model.ClassInfo;
import ubc.cs304.team64.model.Member;


public class ClassViewController implements Initializable {


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
    @FXML
    private Button deregister;
    @FXML
    private Button back;

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
            boolean disable = true;
            String style;
            if(empty || classInfo == null){
              style = "#229aeb";
            }  else if (classInfo.IsOwnerTaking()){
              style = "green";
              disable = false;
            } else if(!member.canTakeClass(classInfo) || classInfo.getCurrentlyTaking() >= classInfo.getCapacity()){
              style = "grey";
            } else {
              style = "#229aeb";
              disable = false;
            }
            setStyle("-fx-background-color: "+style+";");
            setDisable(disable);
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
            if(member.canTakeClass(c) && (c.getCurrentlyTaking() < c.getCapacity() || c.IsOwnerTaking())){
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

        register.disableProperty().bind(new FunctionalBinding<>(mainTable.getSelectionModel().selectedItemProperty(),
            classInfo -> classInfo == null || classInfo.IsOwnerTaking()));
        deregister.disableProperty().bind(new FunctionalBinding<>(mainTable.getSelectionModel().selectedItemProperty(),
            classInfo -> classInfo == null || !classInfo.IsOwnerTaking()));
        register.setOnAction(t -> register(member));
        deregister.setOnAction(t -> deregister(member));

        back.setOnAction(e -> FacilityController.setStage(facility, member));
    }

    public Collection<ClassInfo> SetUp (Facility facility, Member member) {
        Collection<ClassInfo> allClasses = Main.connectionHandler.getClasses(facility, member);
        return allClasses;
    }



    static void setStage(Facility facility, Member member){
        FXMLLoaderWrapper<ClassViewController> loader = new FXMLLoaderWrapper<>("classView.fxml");
        loader.getController().startUp(facility, member);
        Main.updateStage(loader.getScene(), facility.getName());
    }

    private void register(Member member) {
        ClassInfo selected = mainTable.getSelectionModel().getSelectedItem();
        try {
          Main.connectionHandler.registerMemberForClass(selected);
        } catch (IllegalArgumentException e){
          e.printStackTrace();
          // TODO handle
        }
        ClassViewController.setStage(selected.getFacility(), member);
    }

    private void deregister(Member member) {
        ClassInfo selected = mainTable.getSelectionModel().getSelectedItem();
        try {
          Main.connectionHandler.deregisterMemberForClass(selected);
        } catch (IllegalArgumentException e){
          e.printStackTrace();
          // TODO handle
        }
        ClassViewController.setStage(selected.getFacility(), member);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {}
}


