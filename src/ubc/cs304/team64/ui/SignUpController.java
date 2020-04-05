package ubc.cs304.team64.ui;

import javafx.animation.Animation;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import ubc.cs304.team64.model.Member;
import ubc.cs304.team64.model.Payment;
import ubc.cs304.team64.util.FXMLLoaderWrapper;
import ubc.cs304.team64.util.RegexStringConverter;
import ubc.cs304.team64.util.StrokeTransition;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;

public class SignUpController implements Initializable {
  @FXML private TextField name;
  @FXML private DatePicker dob;
  @FXML private TextField street;
  @FXML private TextField city;
  @FXML private ComboBox<String> province;
  @FXML private TextField postalCode;
  @FXML private TextField phoneNumber;
  @FXML private TextField email;
  @FXML private TextField username;
  @FXML private TextField password;
  @FXML private TextField passwordConf;
  @FXML private ComboBox<String> status;
  @FXML private TextField nameOnCard;
  @FXML private TextField cardNumber;
  @FXML private TextField csv;
  @FXML private TextField expiryDate;
  @FXML private Button back;


  private Map<Control, Animation> inputs;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    List<Control> inputList = Arrays.asList(name, dob, street, city, province, postalCode, phoneNumber, email, username, password, passwordConf, status, nameOnCard, cardNumber, csv, expiryDate);
    inputs = new HashMap<>(inputList.size());
    for(Control c : inputList){
      inputs.put(c, StrokeTransition.basicError(c));
    }


    String namePattern = "[A-Z][a-z]*( [A-Z][a-z]*){1,2}";
    name.setTextFormatter(new TextFormatter<>(new RegexStringConverter(namePattern, inputs.get(name), RegexStringConverter::toTitleCase)));

    street.setTextFormatter(new TextFormatter<>(new RegexStringConverter("[^,]*", inputs.get(street))));
    city.setTextFormatter(new TextFormatter<>(new RegexStringConverter("\\w*", inputs.get(street))));

    String postalCodePattern = "([A-Z]\\d){3}";
    postalCode.setTextFormatter(new TextFormatter<>(new RegexStringConverter(postalCodePattern, inputs.get(postalCode), s -> s.replaceAll(" ", "").toUpperCase())));

    phoneNumber.setTextFormatter(new TextFormatter<>(new RegexStringConverter("\\d{10}", inputs.get(phoneNumber), s -> s.replaceAll("[ )(\\-]", ""))));

    email.setTextFormatter(new TextFormatter<>(new RegexStringConverter("[\\w-_.]+@([\\w-.]+\\.)+[\\w]{2,3}", inputs.get(email))));

    username.setTextFormatter(new TextFormatter<>(new RegexStringConverter("\\w{0,20}", inputs.get(username))));

    nameOnCard.setTextFormatter(new TextFormatter<>(new RegexStringConverter(namePattern, inputs.get(nameOnCard), RegexStringConverter::toTitleCase)));

    cardNumber.setTextFormatter(new TextFormatter<>(new RegexStringConverter("\\d{12,19}", inputs.get(cardNumber))));

    csv.setTextFormatter(new TextFormatter<>(new RegexStringConverter("\\d{3}", inputs.get(csv))));

    expiryDate.setTextFormatter(new TextFormatter<>(new RegexStringConverter("(0[1-9]|1[0-2])/\\d{2}", inputs.get(expiryDate), SignUpController::autoCorrectExpiryDate)));

    back.setOnAction(e -> Main.updateStage(new FXMLLoaderWrapper<>("Login.fxml").getScene(), "Login"));

    LocalDate now = LocalDate.now();
    restrictDates(dob, date -> date.isBefore(now));

    Collection<String> statuses = Main.connectionHandler.getStatuses();
    for(String statusString : statuses){
      status.itemsProperty().getValue().add(statusString);
    }
  }

  public void createMember(){
    if(!password.getText().equals(passwordConf.getText())){
      inputs.get(password).playFromStart();
      inputs.get(passwordConf).playFromStart();
      return;
    }
    if(hasNoBlanks()){
      try{
        String expDate = expiryDate.getText();
        Payment createdPayment = Main.connectionHandler.createPayment(
            "Monthly", // TODO fix
            Long.parseLong(cardNumber.getText()),
            Integer.parseInt(csv.getText()),
            LocalDate.of(2000 + Integer.parseInt(expDate.substring(3,5)), Integer.parseInt(expDate.substring(0,2)), 1),
            nameOnCard.getText()
        );

        Member created = Main.connectionHandler.createMember(
            username.getText(),
            password.getText(),
            street.getText() + ", " + city.getText() + ", " + province.getValue() + ", " + postalCode.getText(),
            phoneNumber.getText(),
            email.getText(),
            name.getText(),
            dob.getValue(),
            status.getValue(),
            createdPayment
        );

        FacilitiesController.setStage(created);
      } catch (Exception e){
        inputs.get(username).playFromStart();
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

  private static void restrictDates(DatePicker picker, Predicate<LocalDate> acceptable) {
    picker.setDayCellFactory(d -> new DateCell() {
      public void updateItem(LocalDate item, boolean empty){
        super.updateItem(item, empty);
        setDisable(!acceptable.test(item));
      }
    });
    picker.setConverter(new StringConverter<LocalDate>() {
      @Override
      public String toString(LocalDate localDate) {
        return localDate == null ? "" : localDate.toString();
      }

      @Override
      public LocalDate fromString(String s) {
        LocalDate localDate = LocalDate.parse(s);
        if(!acceptable.test(localDate)){
          throw new IllegalArgumentException();
        }
        return localDate;
      }
    });
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
