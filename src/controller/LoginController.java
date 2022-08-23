package controller;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Appointment;
import model.User;
import util.ConnectDB;
import util.DateTime;

/** FXML Controller class.
 * Controls the login screen.
 * @author Nicholas Fallico
 */
public class LoginController implements Initializable {
    @FXML
    public Label utcLabel;
    @FXML
    public TextField loginEntryBox;
    @FXML
    public PasswordField passwordEntryBox;
    @FXML
    public Button submitButton;
    @FXML
    public Label titleLabel;
    ObservableList<Appointment> upcomingAppList = FXCollections.observableArrayList();
    DateTimeFormatter formatterVariable = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    ZoneId userTime = ZoneId.systemDefault();
    ResourceBundle rb = ResourceBundle.getBundle("properties.login", Locale.getDefault());

    /** Initializes the controller class.
     * @param url The URL parameter.
     * @param rb The ResourceBundle parameter.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("Before rb");
        try {
            rb = ResourceBundle.getBundle("properties.login", Locale.getDefault());
            System.out.println("After rb");
            titleLabel.setText(rb.getString("title"));
            loginEntryBox.setPromptText(rb.getString("username"));
            passwordEntryBox.setPromptText(rb.getString("password"));
            submitButton.setText(rb.getString("signin"));
            utcLabel.setText(ZoneId.systemDefault().toString());
        } catch (MissingResourceException exception) {
            System.out.println("Missing resource");
            exception.printStackTrace();
        }
    }

    /** This method finds meetings starting within 15 minutes and alerts the user. If there are no appointments within
     * 15 minutes there is an alert stating no upcoming meetings in 15 minutes.
     * <p>
     * The lambda on line 94 will locate appointments starting within a quarter of an hour from now. It is justified
     * because the code is more efficient, and therefore the program is more efficient.
     * </p>
     * */
    public void upcomingAlertSignal() {
        System.out.println("**** Being upcomingAlertSignal ****");
        LocalDateTime current = LocalDateTime.now();
        LocalDateTime soon = current.plusMinutes(15);
        System.out.println("Now: " + current);
        System.out.println("NowPlus15: " + soon);
        FilteredList<Appointment> sortedList = new FilteredList<>(upcomingAppList);
        sortedList.setPredicate(row -> {
                    LocalDateTime appointmentTime = LocalDateTime.parse(row.getStart().substring(0, 16), formatterVariable);
                    return appointmentTime.isAfter(current.minusMinutes(1)) && appointmentTime.isBefore(soon);
                }
        );
        if (sortedList.isEmpty()) {
            System.out.println("No upcoming appointment alerts.");
            Alert signal = new Alert(Alert.AlertType.INFORMATION);
            signal.setTitle(rb.getString("noappointment"));
            signal.setHeaderText(rb.getString("noappointment15"));
            signal.setContentText(rb.getString("clickokay"));
            signal.showAndWait();
        } else {
            int appointmentId = sortedList.get(0).getAppointmentID();
            String customer = sortedList.get(0).getCustomerName();
            String dateStart = sortedList.get(0).getStart().substring(0, 10);
            String timeStart = sortedList.get(0).getStart().substring(11, 16);
            Alert signal = new Alert(Alert.AlertType.INFORMATION);
            signal.setTitle(rb.getString("upcomingappointment"));
            signal.setHeaderText(rb.getString("appointmentreminder"));
            signal.setContentText("Appt ID: " + appointmentId + ". " + rb.getString("appointmentwith1") + " " +
                    customer + ". " + rb.getString("appointmentwith2") + " " + timeStart + ", " + dateStart);
            System.out.println("timeStart: " + timeStart);
            System.out.println("dateStart: " + dateStart);
            signal.showAndWait();
        }
        System.out.println("**** End upcomingAlertSignal ****");
    }

    /** This method creates reminder list to be checked with the appointment Alert. */
    public void selectUpcomingApps() {
        System.out.println("**** Begin selectUpcomingApps ****");
        System.out.println(User.getUsername());
        try {
            PreparedStatement sql = ConnectDB.makeConnection().prepareStatement(
                    "SELECT appointments.appointment_Id, appointments.customer_Id, appointments.title, " +
                            "appointments.description, appointments.`start`, appointments.`end`, customers.customer_Id," +
                            " customers.customer_Name, appointments.created_By FROM appointments, customers WHERE " +
                            "appointments.customer_Id = customers.customer_Id AND appointments.created_By = ? "
                            + "ORDER BY `start`");
            sql.setString(1, User.getUsername());
            ResultSet set = sql.executeQuery();
            while (set.next()) {
                Timestamp currentStart = set.getTimestamp("start");
                ZonedDateTime universalStart = currentStart.toLocalDateTime().atZone(ZoneId.of("UTC"));
                ZonedDateTime userTimeStart = universalStart.withZoneSameInstant(userTime);
                Timestamp currentEnd = set.getTimestamp("end");
                ZonedDateTime universalEnd = currentEnd.toLocalDateTime().atZone(ZoneId.of("UTC"));
                ZonedDateTime userTimeEnd = universalEnd.withZoneSameInstant(userTime);
                int appNumber = set.getInt("appointment_Id");
                String appTitle = set.getString("title");
                String description = set.getString("description");
                String custName = set.getString("customer_Name");
                int custID = set.getInt("customer_Id");
                String creator = set.getString("created_By");
                upcomingAppList.add(new Appointment(appNumber, userTimeStart.toString(), userTimeEnd.toString(), appTitle, description, custID, custName, creator));

                //Print to console to check if working correctly
                System.out.println("appNumber: " + appNumber);
                System.out.println("userTimeStart: " + userTimeStart.toString());
                System.out.println("userTimeEnd: " + userTimeEnd.toString());
                System.out.println("Title: " + appTitle);
                System.out.println("description: " + description);
                System.out.println("custID: " + custID);
                System.out.println("custName: " + custName);
                System.out.println("creator: " + creator);
            }
        } catch (SQLException sql) {
            System.out.println("There is an error in your SQL preparedstatement");
            sql.printStackTrace();
        } catch (Exception exception) {
            System.out.println("An error other than your SQL has occurred.");
            exception.printStackTrace();
        }
        System.out.println("**** End create Reminder List ****");
    }

    /** This method handles the submit button.
     * @param event When actioned checks if the login is valid and if so loads the main screen. */
    @FXML
    public void submitButtonHandler(ActionEvent event) throws SQLException, IOException {
        String loginCredential = loginEntryBox.getText();
        String passCredential = passwordEntryBox.getText();
        int creatorID = getCreatorNumber(loginCredential);
        ResourceBundle rb = ResourceBundle.getBundle("properties.login", Locale.getDefault());
        Parent parent;
        Stage setup;
        User user = new User();
        if (passwordChecker(creatorID, passCredential)) {
            User.setUserID(creatorID);
            User.setUsername(loginCredential);
            loginActivity(User.getUsername());
            selectUpcomingApps();
            upcomingAlertSignal();
            parent = FXMLLoader.load(getClass().getResource("/view/MainScreen.fxml"));
            setup = (Stage) submitButton.getScene().getWindow();
            Scene scene = new Scene(parent);
            setup.setScene(scene);
            setup.show();
        } else {
            failedLoginActivity();
            Alert signal = new Alert(Alert.AlertType.INFORMATION);
            signal.setTitle("");
            signal.setHeaderText(rb.getString("loginalertheader"));
            signal.setContentText(rb.getString("loginalertcontent"));
            Optional<ButtonType> result = signal.showAndWait();
        }
    }

    /** This method records each successful login attempt and places them on the file login_activity.txt. 
     * @param user The username of the person logging in.
     * */
    public void loginActivity(String user) {
        try {
            String fileName = "login_activity";
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
            writer.append(DateTime.getTimeStamp() + " - User ID entered: " + user + " - Login successful" + "\n");
            System.out.println("New login recorded in log file.");
            writer.flush();
            writer.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /** This method records each failed login attempt and places them on the file login_activity.txt. */
    public void failedLoginActivity() {
        try {
            String fileName = "login_activity";
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
            writer.append(DateTime.getTimeStamp() + " - User ID entered: " + loginEntryBox.getText() + " - login failed" + "\n");
            System.out.println("New login recorded in log file.");
            writer.flush();
            writer.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /** This method checks if the login password is valid.
     * @param userID The userID of the person logging in.
     * @param password The password of the person logging in.
     * */
    public boolean passwordChecker(int userID, String password) throws SQLException {
        PreparedStatement sql = ConnectDB.makeConnection().prepareStatement("SELECT password FROM users WHERE user_Id ='" + userID + "'");
        ResultSet set = sql.executeQuery();
        while (set.next()) {
            if (set.getString("password").equals(password)) {
                return true;
            }
        }
        return false;
    }

    /** This method obtains the User ID from who is logging in.
     * @param username The username of the person logging in.
     * */
    public int getCreatorNumber(String username) throws SQLException {
        int creatorNumber = -1;

        PreparedStatement sql = ConnectDB.makeConnection().prepareStatement("SELECT user_ID FROM users WHERE " +
                        "user_Name = '" + username + "'");
        ResultSet set = sql.executeQuery();
        while (set.next()) {
            creatorNumber = set.getInt("user_Id");
        }
        return creatorNumber;
    }
}