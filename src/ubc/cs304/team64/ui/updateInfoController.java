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
import java.sql.Date;
import java.sql.SQLOutput;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;

public class updateInfoController implements Initializable {
    private Member member1;
    private Facility facility1;
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
        List<Control> inputList = Arrays.asList(name, street, city, province, email, postalCode, phoneNumber,password, passwordConf, expiryDate, csv, cardNumber, nameOnCard);
        inputs = new HashMap<>(inputList.size());
        for(Control c : inputList){
            inputs.put(c, StrokeTransition.basicError(c));
        }
    }

    public void setUp(Member member, Facility facility){
      member1 = member;
      facility1 = facility;

      String[] addressSplits = member1.getAddress().split(", ");


      String namePattern = "[A-Z][a-z]*( [A-Z][a-z]*){1,2}";
      name.setTextFormatter(new TextFormatter<>(new RegexStringConverter(namePattern, inputs.get(name), RegexStringConverter::toTitleCase)));
      name.setText(member1.getName());

      street.setTextFormatter(new TextFormatter<>(new RegexStringConverter("[^,]*", inputs.get(street))));
      street.setText(addressSplits[0]);
      city.setTextFormatter(new TextFormatter<>(new RegexStringConverter("\\w*", inputs.get(street))));
      city.setText(addressSplits[1]);

      province.getSelectionModel().select(addressSplits[2]);

      String postalCodePattern = "([A-Z]\\d){3}";
      postalCode.setTextFormatter(new TextFormatter<>(new RegexStringConverter(postalCodePattern, inputs.get(postalCode), s -> s.replaceAll(" ", "").toUpperCase())));
      postalCode.setText(addressSplits[3]);

      phoneNumber.setTextFormatter(new TextFormatter<>(new RegexStringConverter("\\d{10}", inputs.get(phoneNumber), s -> s.replaceAll("[ )(\\-]", ""))));
      phoneNumber.setText(member1.getPhoneNumber());

      email.setTextFormatter(new TextFormatter<>(new RegexStringConverter("[\\w-_.]+@([\\w-.]+\\.)+[\\w]{2,3}", inputs.get(email))));
      email.setText(member1.getEmail());

      nameOnCard.setTextFormatter(new TextFormatter<>(new RegexStringConverter(namePattern, inputs.get(nameOnCard), RegexStringConverter::toTitleCase)));

      cardNumber.setTextFormatter(new TextFormatter<>(new RegexStringConverter("\\d{12,19}", inputs.get(cardNumber))));

      csv.setTextFormatter(new TextFormatter<>(new RegexStringConverter("\\d{3}", inputs.get(csv))));

      expiryDate.setTextFormatter(new TextFormatter<>(new RegexStringConverter("(0[1-9]|1[0-2])/\\d{2}", inputs.get(expiryDate), updateInfoController::autoCorrectExpiryDate)));

      back.setOnAction(e -> MyAccountController.setStage(facility1, member1));
    }



    static void setStage(Facility facility, Member member){
        FXMLLoaderWrapper<updateInfoController> loader = new FXMLLoaderWrapper<>("updateInfo.fxml");
        loader.getController().setUp(member, facility);
        Main.updateStage(loader.getScene(), facility.getName());
    }

    public void upDateMember() {
      try{
        Member updated = Main.connectionHandler.updatePersonal(
            member1,
            name.getText(),
            street.getText() + ", " + city.getText() + ", " + province.getValue() + ", " + postalCode.getText(),
            email.getText(),
            phoneNumber.getText());
        setStage(facility1, updated);
      } catch (Exception e){
        //todo
      }
    }

        public void updatePassword() {
            boolean anyNull = false;
            for(TextField c : Arrays.asList(password, passwordConf)){
                if(c.getText().equals("")){
                    inputs.get(c).playFromStart();
                    anyNull = true;
                }
            }
            if(anyNull){
                return;
            }

        if (!password.getText().equals(passwordConf.getText())) {
            inputs.get(password).playFromStart();
            inputs.get(passwordConf).playFromStart();
            return;
        }
                Main.connectionHandler.updatepass(member1, password.getText());
                setStage(facility1, member1);

    }

    public void updatePayment(){
      boolean anyNull = false;
      for(TextField c : Arrays.asList(nameOnCard, csv, expiryDate, cardNumber)){
        if(c.getText().equals("")){
          inputs.get(c).playFromStart();
          anyNull = true;
        }
      }
      if(anyNull){
        return;
      }
      String[] dateSplits = expiryDate.getText().split("/");
      LocalDate date = LocalDate.of(Integer.parseInt(dateSplits[1]), Integer.parseInt(dateSplits[0]), 1);
      Payment payment = Main.connectionHandler.createPayment(
          "Monthly",
          Long.parseLong(cardNumber.getText()),
          Integer.parseInt(csv.getText()),
          date,
          nameOnCard.getText());
      Main.connectionHandler.updatePayment(member1, payment);
      setStage(facility1, member1);
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
