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
    private TableColumn<ClassInfo, Integer> capacityCol;
    @FXML
    private TableColumn<ClassInfo, Integer> instrucCol;

    public void startUp( Facility facility) {
        titleCol.setCellValueFactory(new PropertyValueFactory<ClassInfo, String>("title"));
        DescriptionCol.setCellValueFactory(new PropertyValueFactory<ClassInfo, String>("description"));
        capacityCol.setCellValueFactory(new PropertyValueFactory<ClassInfo, Integer>("capacity"));
        timeCol.setCellValueFactory(new PropertyValueFactory<ClassInfo, Timestamp>("time"));
        instrucCol.setCellValueFactory(new PropertyValueFactory<ClassInfo, Integer>("iid"));

        mainTable.getItems().setAll(SetUp(facility));
    }


    public Collection<ClassInfo> SetUp (Facility facility) {
        Collection<ClassInfo> allClasses = Main.connectionHandler.getClasses(facility);
        return allClasses;
        }



    static void setStage(Facility facility){
        FXMLLoaderWrapper<classViewController> loader = new FXMLLoaderWrapper<>("classView.fxml");
        loader.getController().startUp(facility);
        Main.updateStage(loader.getScene(), facility.getName());
    }
    }


