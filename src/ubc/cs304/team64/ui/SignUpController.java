package ubc.cs304.team64.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class SignUpController implements Initializable {
  @FXML private TextField name;
  @FXML private DatePicker dob;
  @FXML private TextField street;
  @FXML private TextField city;
  @FXML private TextField province;
  @FXML private TextField postalCode;
  @FXML private TextField phoneNumber;
  @FXML private TextField email;
  @FXML private TextField username;
  @FXML private TextField password;
  @FXML private TextField passwordConf;
  @FXML private ComboBox<String> status;
  @FXML private TextField nameOnCard;
  @FXML private TextField cardNumber;
  @FXML private TextField cvv;
  @FXML private TextField expiryDate;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    String postalCodePattern = "([A-Z]\\d){3}";
    postalCode.setTextFormatter(new TextFormatter<>(new RegexStringConverter(postalCodePattern, s -> s.replaceAll(" ", "").toUpperCase())));

    phoneNumber.setTextFormatter(new TextFormatter<>(new RegexStringConverter("\\d{10}", s -> s.replaceAll("[ )(\\-]", ""))));

    email.setTextFormatter(new TextFormatter<>(new RegexStringConverter("[\\w-_.]+@([\\w-]+\\.)+[\\w]{2,3}")));

    username.setTextFormatter(new TextFormatter<>(new RegexStringConverter("\\w{0,20}")));

    nameOnCard.setTextFormatter(new TextFormatter<>(new RegexStringConverter("[A-Z][a-z]*( [A-Z][a-z]*){1,2}", RegexStringConverter::toTitleCase)));

    cardNumber.setTextFormatter(new TextFormatter<>(new RegexStringConverter("\\d{12,19}")));

    cvv.setTextFormatter(new TextFormatter<>(new RegexStringConverter("\\d{3}")));

    expiryDate.setTextFormatter(new TextFormatter<>(new RegexStringConverter("(0[1-9]|1[0-2])/\\d{2}", SignUpController::autoCorrectExpiryDate)));

    LocalDate now = LocalDate.now();
    restrictDates(dob, date -> date.isBefore(now));

    Collection<String> statuses = Main.connectionHandler.getStatuses();
    for(String statusString : statuses){
      status.itemsProperty().getValue().add(statusString);
    }
  }

  private void restrictDates(DatePicker picker, Predicate<LocalDate> acceptable) {
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
