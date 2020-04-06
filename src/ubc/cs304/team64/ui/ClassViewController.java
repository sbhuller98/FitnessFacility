package ubc.cs304.team64.ui;

import javafx.animation.PauseTransition;
import javafx.beans.binding.Binding;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ResourceBundle;

import javafx.scene.control.*;
import javafx.util.Duration;
import ubc.cs304.team64.model.ClassColumn;
import ubc.cs304.team64.model.Facility;
import ubc.cs304.team64.model.ClassInfo;
import ubc.cs304.team64.model.Member;
import ubc.cs304.team64.util.FXMLLoaderWrapper;
import ubc.cs304.team64.util.FunctionalBinding;
import ubc.cs304.team64.util.ImmutablePropertyFactory;


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

    @FXML private ComboBox<ClassColumn> columnSelector;
    @FXML private TextField valueSelector;
    @FXML private Button filterButton;

    private Member member;
    private Facility facility;

    public void startUp(Facility facility, Member member) {
        this.member = member;
        this.facility = facility;

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
        register.setOnAction(t -> register());
        deregister.setOnAction(t -> deregister());

        filterButton.disableProperty().bind(columnSelector.valueProperty().isNull());
        valueSelector.disableProperty().bind(new FunctionalBinding<>(columnSelector.valueProperty(), c -> c == null || c == ClassColumn.NONE));
        columnSelector.setItems(FXCollections.observableList(Arrays.asList(ClassColumn.values())));
        filterButton.setOnAction(e -> filter());

        back.setOnAction(e -> FacilityController.setStage(facility, member));
    }



    static void setStage(Facility facility, Member member){
        FXMLLoaderWrapper<ClassViewController> loader = new FXMLLoaderWrapper<>("classView.fxml");
        loader.getController().startUp(facility, member);
        Main.updateStage(loader.getScene(), facility.getName());
    }

    private void register() {
        ClassInfo selected = mainTable.getSelectionModel().getSelectedItem();
        try {
          Main.connectionHandler.registerMemberForClass(selected);
        } catch (IllegalArgumentException e){
          e.printStackTrace();
          // TODO handle
        }
        filter();
    }

    private void deregister() {
        ClassInfo selected = mainTable.getSelectionModel().getSelectedItem();
        try {
          Main.connectionHandler.deregisterMemberForClass(selected);
        } catch (IllegalArgumentException e){
          e.printStackTrace();
          // TODO handle
        }
        filter();
    }

    private void filter() {
      mainTable.setItems(FXCollections.observableList(Main.connectionHandler.getClasses(facility, member, columnSelector.getValue(), valueSelector.getText())));
      mainTable.refresh();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {}
}


