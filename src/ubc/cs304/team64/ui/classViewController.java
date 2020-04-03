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

        mainTable.getItems().setAll(SetUp(facility));
    }


    public Collection<ClassInfo> SetUp (Facility facility) {
        Collection<ClassInfo> allClasses = Main.connectionHandler.getClasses(facility);
        return allClasses;
        }



    static void setStage(Facility facility, Member member){
        FXMLLoaderWrapper<classViewController> loader = new FXMLLoaderWrapper<>("classView.fxml");
        loader.getController().startUp(facility, member);
        Main.updateStage(loader.getScene(), facility.getName());
    }
    }


