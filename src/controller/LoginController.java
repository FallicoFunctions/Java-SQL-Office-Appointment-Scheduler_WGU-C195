package controller;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
    private Label utcLabel;
    @FXML
    private TextField loginEntryBox;
    @FXML
    private PasswordField passwordEntryBox;
    @FXML
    private Button submitButton;
    @FXML
    private Label titleLabel;
    ObservableList<Appointment> appointmentReminderOL = FXCollections.observableArrayList();
    private DateTimeFormatter datetimeDTF = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private ZoneId localTime = ZoneId.systemDefault();
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
        } catch (MissingResourceException e) {
            System.out.println("Missing resource");
        }
    }

    /** This method filters the reminder list and alerts if appointment is within 15 minutes.
     * <p>
     * The lambda in this method will identify any appointments starting in the next 15 minutes. It is justified because
     * the code is more efficient, and therefore the program is more efficient.
     * </p>
     * */
    private void appointmentAlert() {
        System.out.println("**** Being appointmentAlert ****");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nowPlus15Min = now.plusMinutes(15);
        System.out.println("Now: " + now);
        System.out.println("NowPlus15: " + nowPlus15Min);

        FilteredList<Appointment> filteredData = new FilteredList<>(appointmentReminderOL);

        //lambda expression used to efficiently identify any appointment starting within the next 15 minutes
        filteredData.setPredicate(row -> {
                    LocalDateTime rowDate = LocalDateTime.parse(row.getStart().substring(0, 16), datetimeDTF);
                    return rowDate.isAfter(now.minusMinutes(1)) && rowDate.isBefore(nowPlus15Min);
                }
        );
        if (filteredData.isEmpty()) {
            System.out.println("No upcoming appointment alerts.");
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(rb.getString("noappointment"));
            alert.setHeaderText(rb.getString("noappointment15"));
            alert.setContentText(rb.getString("clickokay"));
            alert.showAndWait();
        } else {
            String type = filteredData.get(0).getDescription();
            String customer = filteredData.get(0).getCustomerName();
            String start = filteredData.get(0).getStart().substring(0, 16);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(rb.getString("upcomingappointment"));
            alert.setHeaderText(rb.getString("appointmentreminder"));
            alert.setContentText(rb.getString("appointmentwith1") + customer
                    + rb.getString("appointmentwith2") + start + ".");
            alert.showAndWait();
        }
        System.out.println("**** End appointmentAlert ****");
    }

    /** This method creates reminder list to be checked with the appointment Alert. */
    private void createReminderList() {
        System.out.println("**** Begin createReminderList ****");
        System.out.println(User.getUsername());
        try {
            PreparedStatement ps = ConnectDB.makeConnection().prepareStatement(
                    "SELECT appointments.appointment_Id, appointments.customer_Id, appointments.title, appointments.description, "
                            + "appointments.`start`, appointments.`end`, customers.customer_Id, customers.customer_Name, appointments.created_By "
                            + "FROM appointments, customers "
                            + "WHERE appointments.customer_Id = customers.customer_Id AND appointments.created_By = ? "
                            + "ORDER BY `start`");
            ps.setString(1, User.getUsername());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                //pulls start time from database and converts it into local time zone
                Timestamp timestampStart = rs.getTimestamp("start");
                ZonedDateTime startUTC = timestampStart.toLocalDateTime().atZone(ZoneId.of("UTC"));
                ZonedDateTime newLocalStart = startUTC.withZoneSameInstant(localTime);

                //pulls end time from database and converts it into local time zone
                Timestamp timestampEnd = rs.getTimestamp("end");
                ZonedDateTime endUTC = timestampEnd.toLocalDateTime().atZone(ZoneId.of("UTC"));
                ZonedDateTime newLocalEnd = endUTC.withZoneSameInstant(localTime);

                //pulls select data fields for use in appointmentReminderOL observablelist
                int appointmentId = rs.getInt("appointment_Id");
                String title = rs.getString("title");
                String type = rs.getString("description");
                String customerName = rs.getString("customer_Name");
                int customerId = rs.getInt("customer_Id");
                String user = rs.getString("created_By");

                //prints values of data fields prior to being inserted into observablelist
                System.out.println("AppointmentID: " + appointmentId);
                System.out.println("newLocalStart: " + newLocalStart.toString());
                System.out.println("newLocalEnd: " + newLocalEnd.toString());
                System.out.println("Title: " + title);
                System.out.println("Type: " + type);
                System.out.println("CustomerId: " + customerId);
                System.out.println("CustomerName: " + customerName);
                System.out.println("User: " + user);

                //inserts Appointment objects into observablelist
                appointmentReminderOL.add(new Appointment(appointmentId, newLocalStart.toString(), newLocalEnd.toString(), title, type, customerId, customerName, user));
            }

        } catch (SQLException sqe) {
            System.out.println("There is an error in your SQL preparedstatement");
            sqe.printStackTrace();
        } catch (Exception e) {
            System.out.println("An error other than your SQL has occurred.");
            e.printStackTrace();
        }
        System.out.println("**** End create Reminder List ****");
    }

    /** This method handles the login button.
     * @param event When actioned checks if the login is valid and if so loads the main screen. */
    @FXML
    private void submitButtonHandler(ActionEvent event) throws SQLException, IOException {
        String usernameInput = loginEntryBox.getText();
        String passwordInput = passwordEntryBox.getText();
        int userID = getUserID(usernameInput);
        ResourceBundle rb = ResourceBundle.getBundle("properties.login", Locale.getDefault());
        Parent parent;
        Stage setup;
        User user = new User();

        if (isValidPassword(userID, passwordInput)) {
            User.setUserID(userID);
            User.setUsername(usernameInput);

            //calls method to write current user to the log
            loginLog(User.getUsername());
            createReminderList();
            appointmentAlert();

            //calls mainscreen scene after successful login
            parent = FXMLLoader.load(getClass().getResource("/view/MainScreen.fxml"));
            setup = (Stage) submitButton.getScene().getWindow();
            Scene scene = new Scene(parent);
            setup.setScene(scene);
            setup.show();
        } else {
            loginLogFail();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("");
            alert.setHeaderText(rb.getString("loginalertheader"));
            alert.setContentText(rb.getString("loginalertcontent"));
            Optional<ButtonType> result = alert.showAndWait();
        }
    }

    /** This method creates a new log file if one doesn't exist and inserts successful login information for current user.
     * @param user The username of the person logging in.
     * */
    public void loginLog(String user) {
        try {
            String fileName = "login_activity";
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
            writer.append(DateTime.getTimeStamp() + " - User ID entered: " + user + " - Login successful" + "\n");
            System.out.println("New login recorded in log file.");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /** This method creates a new log file if one doesn't exist and inserts failed login information for current user. */
    public void loginLogFail() {
        try {
            String fileName = "login_activity";
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
            writer.append(DateTime.getTimeStamp() + " - User ID entered: " + loginEntryBox.getText() + " - login failed" + "\n");
            System.out.println("New login recorded in log file.");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /** This method checks if the login password is valid.
     * @param userID The userID of the person logging in.
     * @param password The password of the person logging in.
     * */
    private boolean isValidPassword(int userID, String password) throws SQLException {
        //create statement object
        Statement statement = ConnectDB.conn.createStatement();

        //write SQL statement
        String sqlStatement = "SELECT password FROM users WHERE user_Id ='" + userID + "'";;

        //create resultset object
        ResultSet result = statement.executeQuery(sqlStatement);

        while (result.next()) {
            if (result.getString("password").equals(password)) {
                return true;
            }
        }
        return false;
    }

    /** This method gets the User ID for current user.
     * @param username The username of the person logging in.
     * */
    private int getUserID(String username) throws SQLException {
        int userID = -1;

        //create statement object
        Statement statement = ConnectDB.conn.createStatement();

        //write SQL statement
        String sqlStatement = "SELECT user_ID FROM users WHERE user_Name ='" + username + "'";

        //create resultset object
        ResultSet result = statement.executeQuery(sqlStatement);

        while (result.next()) {
            userID = result.getInt("user_Id");
        }
        return userID;
    }
}
