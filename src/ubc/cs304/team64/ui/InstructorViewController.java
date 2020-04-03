package ubc.cs304.team64.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.ResourceBundle;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import ubc.cs304.team64.model.Facility;
import ubc.cs304.team64.model.ClassInfo;
import ubc.cs304.team64.model.Instructor;


public class InstructorViewController implements Initializable {



    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
    @FXML
    private TableView<Instructor> mainTable1;
    @FXML
    private TableColumn<Instructor, Integer> iidCol;
    @FXML
    private TableColumn<Instructor, String> iNameCol;
    @FXML
    private TableColumn<Instructor, Double> averageCol;
    @FXML
    private TableColumn<Instructor, Double> salaryCol;


    public void startUp(Facility facility) {
        iidCol.setCellValueFactory(new PropertyValueFactory<Instructor, Integer>("iid"));
        iNameCol.setCellValueFactory(new PropertyValueFactory<Instructor, String>("name"));
        averageCol.setCellValueFactory(new PropertyValueFactory<Instructor, Double>("averageRating"));
        salaryCol.setCellValueFactory(new PropertyValueFactory<Instructor, Double>("salary"));

        mainTable1.getItems().setAll(SetUp(facility));
    }


    public Collection<Instructor> SetUp (Facility facility) {
        Collection<Instructor> allClasses = Main.connectionHandler.getInstructorsFromFacility(facility);
        return allClasses;
    }



    static void setStage(Facility facility){
        FXMLLoaderWrapper<InstructorViewController> loader1 = new FXMLLoaderWrapper<>("instructorView.fxml");
        loader1.getController().startUp(facility);
        Main.updateStage(loader1.getScene(), facility.getName());
    }
}

