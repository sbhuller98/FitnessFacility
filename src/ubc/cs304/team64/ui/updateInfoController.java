package ubc.cs304.team64.ui;

import javafx.animation.Animation;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import ubc.cs304.team64.model.Facility;
import ubc.cs304.team64.model.Member;
import ubc.cs304.team64.model.Payment;
import ubc.cs304.team64.util.FXMLLoaderWrapper;
import ubc.cs304.team64.util.RegexStringConverter;
import ubc.cs304.team64.util.StrokeTransition;

import java.net.URL;
import java.sql.SQLOutput;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;

public class updateInfoController implements Initializable {
    private static Member member1;
    private static Facility facility1;
    @FXML private TextField name;
    @FXML private TextField street;
    @FXML private TextField city;
    @FXML private ComboBox<String> province;
    @FXML private TextField postalCode;
    @FXML private TextField phoneNumber;
    @FXML private TextField email;
    @FXML private TextField password;
    @FXML private TextField passwordConf;
    @FXML private TextField nameOnCard;
    @FXML private TextField cardNumber;
    @FXML private TextField csv;
    @FXML private TextField expiryDate;
    @FXML private Button back;
    @FXML private Button upDatepButton;


    private Map<Control, Animation> inputs;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        List<Control> inputList = Arrays.asList(name, street, city, province, postalCode, phoneNumber, email, password, passwordConf, nameOnCard, cardNumber, csv, expiryDate);
        inputs = new HashMap<>(inputList.size());
        for(Control c : inputList){
            inputs.put(c, StrokeTransition.basicError(c));
        }


        String namePattern = "[A-Z][a-z]*( [A-Z][a-z]*){1,2}";
        name.setTextFormatter(new TextFormatter<>(new RegexStringConverter(namePattern, inputs.get(name), RegexStringConverter::toTitleCase)));

        String postalCodePattern = "([A-Z]\\d){3}";
        postalCode.setTextFormatter(new TextFormatter<>(new RegexStringConverter(postalCodePattern, inputs.get(postalCode), s -> s.replaceAll(" ", "").toUpperCase())));

        phoneNumber.setTextFormatter(new TextFormatter<>(new RegexStringConverter("\\d{10}", inputs.get(phoneNumber), s -> s.replaceAll("[ )(\\-]", ""))));

        email.setTextFormatter(new TextFormatter<>(new RegexStringConverter("[\\w-_.]+@([\\w-.]+\\.)+[\\w]{2,3}", inputs.get(email))));

        nameOnCard.setTextFormatter(new TextFormatter<>(new RegexStringConverter(namePattern, inputs.get(nameOnCard), RegexStringConverter::toTitleCase)));

        cardNumber.setTextFormatter(new TextFormatter<>(new RegexStringConverter("\\d{12,19}", inputs.get(cardNumber))));

        csv.setTextFormatter(new TextFormatter<>(new RegexStringConverter("\\d{3}", inputs.get(csv))));

        expiryDate.setTextFormatter(new TextFormatter<>(new RegexStringConverter("(0[1-9]|1[0-2])/\\d{2}", inputs.get(expiryDate), updateInfoController::autoCorrectExpiryDate)));

        back.setOnAction(e -> MyAccountController.setStage(facility1, member1));



    }



    static void setStage(Facility facility, Member member){

        FXMLLoaderWrapper<updateInfoController> loader = new FXMLLoaderWrapper<>("updateInfo.fxml");
        member1 = member;
        facility1 = facility;
        Main.updateStage(loader.getScene(), facility.getName());
    }

    public void upDateMember() {
        if(hasNoBlanks()){
            try{
                /*Main.connectionHandler.updatePersonal(
                        member1.getMid(),
                        name.getText(),
                        street.getText() + ", " + city.getText() + ", " + province.getValue() + ", " + postalCode.getText(),
                        email.getText(), phoneNumber.getText());*/

            } catch (Exception e){
                //todo
            }
        }
    }

    private boolean hasNoBlanks() {
        boolean retVal = true;
        for(Map.Entry<Control, Animation> entry : inputs.entrySet()){
            Object data;
            if(entry.getKey() instanceof ComboBoxBase){
                data = ((ComboBoxBase)(entry.getKey())).getValue();
            } else if(entry.getKey() instanceof TextInputControl){
                data = ((TextInputControl)(entry.getKey())).getText();
                if(data.equals("")) data = null;
            } else {
                data = "Something";
            }
            if(data == null){
                entry.getValue().playFromStart();
                retVal = false;
            }
        }
        return retVal;
    }

    private static String autoCorrectExpiryDate(String expiryDate){
        expiryDate = expiryDate.replaceAll("/", "");
        if(expiryDate.length() <= 3){
            if(expiryDate.length() <= 2){
                return "";
            }
            expiryDate = "0" + expiryDate;
        }
        return expiryDate.substring(0,2) + "/" + expiryDate.substring(2,4);
    }
}
