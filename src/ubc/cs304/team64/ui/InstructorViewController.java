package ubc.cs304.team64.ui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;

import javafx.scene.control.*;
import ubc.cs304.team64.model.Facility;
import ubc.cs304.team64.model.Instructor;
import ubc.cs304.team64.model.Member;
import ubc.cs304.team64.util.FXMLLoaderWrapper;
import ubc.cs304.team64.util.ImmutablePropertyFactory;


public class InstructorViewController implements Initializable {

    @FXML
    private TableView<Instructor> mainTable1;
    @FXML
    private TableColumn<Instructor, String> iNameCol;
    @FXML
    private TableColumn<Instructor, Double> averageCol;
    @FXML
    private TableColumn<Instructor, Double> salaryCol;
    @FXML
    private TableColumn<Instructor, String> memberRatingCol;

    @FXML
    private Slider rating;
    @FXML
    private Button rate;

    @FXML private Button back;
    @FXML private ComboBox<String> iViews;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }



    public void startUp(Facility facility, Member member) {
        back.setOnAction(e -> FacilityController.setStage(facility, member));
        iNameCol.setCellValueFactory(new ImmutablePropertyFactory<>(Instructor::getName));
        averageCol.setCellValueFactory(new ImmutablePropertyFactory<>(Instructor::getAverageRating));
        salaryCol.setCellValueFactory(new ImmutablePropertyFactory<>(Instructor::getSalary));
        memberRatingCol.setCellValueFactory(new ImmutablePropertyFactory<>(Instructor::getMembersRating));
        mainTable1.getItems().setAll(SetUp(facility, member));
        mainTable1.getSelectionModel().selectedItemProperty().addListener((observableValue, old, next) -> {
          rating.setDisable(next == null);
          if(next == null){
            return;
          }
          if(next.getMembersRating().equals("")){
            rating.setValue(3.0);
          } else {
            rating.setValue(Integer.parseInt(next.getMembersRating()));
          }
        });
        rate.disableProperty().bind(mainTable1.getSelectionModel().selectedItemProperty().isNull());
        rate.setOnAction(e -> {
          Main.connectionHandler.rateInstructor(member, mainTable1.getSelectionModel().getSelectedItem(), (int)rating.getValue());
          setStage(facility, member);
        });
    }


    public Collection<Instructor> SetUp (Facility facility, Member m) {
        Collection<Instructor> allClasses = Main.connectionHandler.getInstructorsFromFacility(facility, m);
        return allClasses;
    }



    static void setStage(Facility facility, Member member){
        FXMLLoaderWrapper<InstructorViewController> loader1 = new FXMLLoaderWrapper<>("instructorView.fxml");
        loader1.getController().startUp(facility, member);
        Main.updateStage(loader1.getScene(), facility.getName());
    }
}

