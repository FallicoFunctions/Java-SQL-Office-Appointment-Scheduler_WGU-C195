package controller;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Appointment;
import model.Customer;
import model.User;
import util.ConnectDB;

/** FXML Controller class.
 * Controls the Add Appointment screen.
 * @author Nicholas Fallico
 */
public class AddAppointmentController implements Initializable {
    @FXML
    public Button buttonToCancel;
    @FXML
    public ComboBox<String> comboBoxType;
    @FXML
    public ComboBox<String> comboBoxLocation;
    @FXML
    public TextField appIDEntryBox;
    @FXML
    public TextField custIDEntryBox;
    @FXML
    public TextField userIDEntryBox;
    @FXML
    public Label appUpdateLabel;
    @FXML
    public TextField titleEntryBox;
    @FXML
    public TextField descriptionEntryBox;
    @FXML
    public DatePicker dateStartPicker;
    @FXML
    public ComboBox<String> comboBoxContact;
    @FXML
    public ComboBox<String> comboBoxStart;
    @FXML
    public ComboBox<String> comboBoxEnd;
    @FXML
    public Button buttonToSave;
    @FXML
    public TableView<Customer> custListDisplay;
    @FXML
    public TableColumn<Customer, Integer> colCustID;
    @FXML
    public TableColumn<Customer, String> colCustName;

    //Declare variables and lists
    DateTimeFormatter stageFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    DateTimeFormatter dateStageFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    ObservableList<Customer> listOfCusts = FXCollections.observableArrayList();
    ObservableList<String> listOfBeginningMeetingTimes = FXCollections.observableArrayList();
    ObservableList<String> listOfEndingMeetingTimes = FXCollections.observableArrayList();
    ObservableList<String> contactList = FXCollections.observableArrayList();
    ResourceBundle rb = ResourceBundle.getBundle("properties.login", Locale.getDefault());
    ZoneId userTime = ZoneId.systemDefault();
    Customer chosenCust = new Customer();
    Appointment chosenMeeting;
    Parent parent;
    Stage setup;

    /** Initializes the controller class.
     * @param url The URL parameter.
     * @param rb The ResourceBundle parameter.
     * <p>
     * There is a lambda listener expression on line 128. It is justified because it listens for the mouse click in the
     * customer table. Then it populates the add appointment form with the selected customer's info. It is more efficient
     * to use a lambda for this task.
     * </p>
     * <p>
     * There is a lambda expression on line 135. It is justified because it disables Saturdays and Sundays from being 
     * selected in the calendar selector. It is more efficient to use a lambda for this task.
     * </p>
     * */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colCustID.setCellValueFactory(new PropertyValueFactory<>("CustomerID"));
        colCustName.setCellValueFactory(new PropertyValueFactory<>("CustomerName"));
        custIDEntryBox.setEditable(false);
        try {
            loadListOfCusts();
        } catch (SQLException ex) {
            System.out.println("Something is wrong with your SQL code!");
        }
        try {
            fillComboBoxType();
            fillComboBoxContact();
            fillComboBoxLocation();
            fillMeetingTimes();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        custListDisplay.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, custData) -> displayCustName(custData));
        System.out.println("Current User is: " + User.getUsername());
        appIDEntryBox.setText("Auto Generated");
        appIDEntryBox.setDisable(true);
        userIDEntryBox.setDisable(true);

        dateStartPicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate day, boolean bool) {
                super.updateItem(day, bool);
                if (day.getDayOfWeek() == DayOfWeek.SATURDAY) {
                    setDisable(true);
                    setStyle("-fx-background-color: #708090;");
                }
                if (day.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    setDisable(true);
                    setStyle("-fx-background-color: #708090;");
                }
            }
        });
        //This code checks to see if we are updating a current customer or creating a new one
        if (MainAppointmentsController.addUpdateFilter == 2) {
            chosenMeeting = MainAppointmentsController.getReviseApp();
            chosenCust.setCustomerID(chosenMeeting.getCustomerID());
            System.out.println("chosenCustID: " + chosenCust.getCustomerID());
            custListDisplay.getSelectionModel().select(chosenCust);
            inputAppData();
        }
    }

    /** This method sets customer name into the add appointment form.
     * @param custData Cust selected and used to obtain cust name and ID.
     * */
    public void displayCustName(Customer custData) {
        custIDEntryBox.setText(custData.getCustomerName());
        chosenCust = custData;
        appIDEntryBox.setText("Auto Generated");
        appIDEntryBox.setDisable(true);
        userIDEntryBox.setDisable(true);
        userIDEntryBox.setText(String.valueOf(User.getUserID()));
    }

    /** This method inputs appointment data into the entry fields */
    public void inputAppData() {
        System.out.println("**** Start Update Appointment Fields ****");
        appUpdateLabel.setText("Update Appointment");
        System.out.println("Selected Appointment CustomerID: " + chosenMeeting.getCustomerID());
        chosenCust.setCustomerID(chosenMeeting.getCustomerID());
        String startLocal = chosenMeeting.getStart();
        System.out.println("Local Start: " + startLocal);
        String endLocal = chosenMeeting.getEnd();
        System.out.println("Local End: " + endLocal);
        LocalDateTime localDateTimeStart = LocalDateTime.parse(startLocal, dateStageFormatter);
        LocalDateTime localDateTimeEnd = LocalDateTime.parse(endLocal, dateStageFormatter);
        LocalDate localDate = localDateTimeStart.toLocalDate();
        System.out.println("localDate: " + localDate);
        LocalTime localTimeStart = localDateTimeStart.toLocalTime();
        System.out.println("localTimeStart: " + localTimeStart);

        //Inputs appointment data from the chosen meeting
        custIDEntryBox.setText(chosenMeeting.getCustomerName());
        titleEntryBox.setText(chosenMeeting.getTitle());
        descriptionEntryBox.setText(chosenMeeting.getDescription());
        comboBoxType.setValue(chosenMeeting.getType());
        comboBoxContact.setValue(getContactName(chosenMeeting.getContact()));
        comboBoxLocation.setValue(chosenMeeting.getLocation());
        dateStartPicker.setValue(localDate);
        comboBoxStart.getSelectionModel().select(localDateTimeStart.toLocalTime().format(stageFormatter));
        comboBoxEnd.getSelectionModel().select(localDateTimeEnd.toLocalTime().format(stageFormatter));
        System.out.println("**** End Update Appointment Fields ****");
    }

    /** This method loads the customer list into the tableview. */
    public void loadListOfCusts() throws SQLException {
        System.out.println("**** Start update Customer Table ****");
        Statement stmt = ConnectDB.makeConnection().createStatement();
        String sqlStatement = "SELECT customer_Id, customer_Name FROM customers";
        ResultSet set = stmt.executeQuery(sqlStatement);
        while (set.next()) {
            Customer cust = new Customer();
            cust.setCustomerName(set.getString("customer_Name"));
            cust.setCustomerID(set.getInt("customer_Id"));
            listOfCusts.addAll(cust);
        }
        custListDisplay.setItems(listOfCusts);
        System.out.println("**** End update Customer Table ****");
    }

    /** This method clears appointment text fields. */
    public void emptyDataEntries() {
        System.out.println("**** Start Clear Appointment Fields ****");
        custIDEntryBox.setText("");
        titleEntryBox.setText("");
        descriptionEntryBox.setText("");
        comboBoxType.getSelectionModel().clearSelection();;
        comboBoxContact.getSelectionModel().clearSelection();
        comboBoxLocation.getSelectionModel().clearSelection();
        dateStartPicker.setValue(null);
        comboBoxStart.getSelectionModel().clearSelection();
        comboBoxEnd.getSelectionModel().clearSelection();
        System.out.println("**** End Clear Appointment Fields ****");
    }

    /** This method fills the type combobox with preselected options. */
    public void fillComboBoxType() {
        ObservableList<String> meetingTypeOL = FXCollections.observableArrayList();
        meetingTypeOL.addAll("First-time Patient", "General Checkup", "Blood Work Visit", "Psychiatric Visit");
        comboBoxType.setItems(meetingTypeOL);
    }

    /** This method fills the location combobox with preselected locations. */
    public void fillComboBoxLocation() {
        ObservableList<String> meetingLocationOL = FXCollections.observableArrayList();
        meetingLocationOL.addAll("Phoenix", "White Plains", "Montreal", "London");
        comboBoxLocation.setItems(meetingLocationOL);
    }

    /** This method fills the time picker with times during office hours.
     * Office hours are set to 8am-10pm eastern standard time.
     * The minimum time for a meeting is 15 minutes.
     * */
    public void fillMeetingTimes() {
        LocalTime stage = LocalTime.of(8, 0, 0);
        do {
            listOfBeginningMeetingTimes.add(stage.format(stageFormatter));
            listOfEndingMeetingTimes.add(stage.format(stageFormatter));
            stage = stage.plusMinutes(15);
        } while (!stage.equals(LocalTime.of(21, 30, 0)));
        listOfBeginningMeetingTimes.remove(listOfBeginningMeetingTimes.size() - 1);
        listOfEndingMeetingTimes.remove(0);
        if (MainAppointmentsController.addUpdateFilter != 2) {
            dateStartPicker.setValue(LocalDate.now());
        }
        comboBoxStart.setItems(listOfBeginningMeetingTimes);
        comboBoxEnd.setItems(listOfEndingMeetingTimes);
        comboBoxStart.getSelectionModel().select(LocalTime.of(8, 0, 0).format(stageFormatter));
        comboBoxEnd.getSelectionModel().select(LocalTime.of(8, 30, 0).format(stageFormatter));
    }

    /** This method populates contact list with names of contacts used to set appointment. */
    public void fillComboBoxContact() throws SQLException {
        Statement stmt = ConnectDB.makeConnection().createStatement();
        String sqlStatement = "SELECT contact_name FROM contacts";
        ResultSet set = stmt.executeQuery(sqlStatement);
        while (set.next()) {
            Appointment app = new Appointment();
            app.setContact(set.getString("contact_name"));
            contactList.add(app.getContact());
            comboBoxContact.setItems(contactList);
        }
        stmt.close();
        set.close();
    }

    /** Thie method gets the contact name based on their contact ID.
     * @param contactID The contact's ID number.
     * @return Returns the contact's name.
     * */
    public String getContactName(String contactID) {
        String contactName = "";
        String sqlStatement = "SELECT contact_name FROM contacts WHERE contact_Id = ?";
        System.out.println(sqlStatement);
        try {
            PreparedStatement sql = ConnectDB.makeConnection().prepareStatement(sqlStatement);
            sql.setString(1, contactID);
            System.out.println(contactID);
            ResultSet set = sql.executeQuery();
            while (set.next()) {
                contactName = set.getString("contact_name");
            }
            System.out.println(contactName);
        } catch (SQLException throwables){
            throwables.printStackTrace();
        }
        return contactName;
    }

    /** This method gets the contact's ID based on their name.
     * @param contact_name The contact's number.
     * @return Returns the contact's ID.
     * */
    public int getContactID(String contact_name) {
        int contactID = -1;
        String sqlStatement = "SELECT contact_Id FROM contacts WHERE contact_name = ?";
        System.out.println(sqlStatement);
        try {
            PreparedStatement sql = ConnectDB.makeConnection().prepareStatement(sqlStatement);
            sql.setString(1, contact_name);
            System.out.println(contact_name);
            ResultSet set = sql.executeQuery();
            while (set.next()) {
                contactID = set.getInt("contact_id");
            }
            System.out.println(contactID);
        } catch (SQLException throwables){
            throwables.printStackTrace();
        }
        return contactID;
    }

    /** This method checks if the appointment's fields are valid.
     * @return Returns False or True based on validity checks.
     * */
    public boolean meetingValidator() {
        System.out.println("****** Begin Appointment Validation *****");
        Customer current = custListDisplay.getSelectionModel().getSelectedItem();

        String title = titleEntryBox.getText();
        System.out.println("title: " + title);

        String description = descriptionEntryBox.getText();
        System.out.println("description: " + description);

        String meetingType = comboBoxType.getValue();
        System.out.println("meetingType: " + meetingType);

        String nameOfContact = comboBoxContact.getValue();
        System.out.println("nameOfContact: " + nameOfContact);

        String location = comboBoxLocation.getValue();
        System.out.println("nameOfContact: " + nameOfContact);

        LocalDate userMeetingDay = dateStartPicker.getValue();
        System.out.println("meeting day: " + userMeetingDay);

        LocalTime meetingBeginning = LocalTime.parse(comboBoxStart.getSelectionModel().getSelectedItem(), stageFormatter);
        System.out.println("meeting start: " + meetingBeginning);

        LocalTime meetingEnding = LocalTime.parse(comboBoxEnd.getSelectionModel().getSelectedItem(), stageFormatter);
        System.out.println("meeting end: " + meetingEnding);

        LocalDateTime dateOfMeetingStart = LocalDateTime.of(userMeetingDay, meetingBeginning);
        System.out.println("date of meeting: " + dateOfMeetingStart);

        LocalDateTime dateOfMeetingEnd = LocalDateTime.of(userMeetingDay, meetingEnding);
        System.out.println("date of meeting: " + dateOfMeetingEnd);

        ZonedDateTime universalStart = dateOfMeetingStart.atZone(userTime).withZoneSameInstant(ZoneId.of("UTC"));
        System.out.println("UTC meeting start: " + universalStart);

        ZonedDateTime universalEnd = dateOfMeetingEnd.atZone(userTime).withZoneSameInstant(ZoneId.of("UTC"));
        System.out.println("UTC meeting end: " + universalEnd);

        //The below if-statements verify if the appointment entry fields are blank
        String isValid = "";
        if (meetingType == null) {
            isValid += rb.getString("appointmenttype") + System.lineSeparator();
        }
        if (current == null) {
            isValid += rb.getString("customerselect") + System.lineSeparator();
        }
        if (title.length() == 0) {
            isValid += rb.getString("appointmenttitle") + System.lineSeparator();
        }
        if (description.length() == 0) {
            isValid += rb.getString("appointmentdescription") + System.lineSeparator();
        }
        if (nameOfContact == null) {
            isValid += rb.getString("appointmentcontact") + System.lineSeparator();
        }
        if (location == null) {
            isValid += rb.getString("appointmentlocation") + System.lineSeparator();
        }
        if (universalEnd.equals(universalStart)) {
            isValid += rb.getString("endstarttime") + System.lineSeparator();
        }
        if (universalEnd.isBefore(universalStart)) {
            isValid += rb.getString("endstarttime");
        }
        else {
            try {
                //checks user's existing appointments for time conflicts
                if (validateMeetings(universalStart, universalEnd)) {
                    isValid += rb.getString("apptimeconflict") + System.lineSeparator();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        if (isValid.length() == 0) {
            return true;
        } else {
            Alert signal = new Alert(Alert.AlertType.ERROR);
            signal.setTitle(rb.getString("error"));
            signal.setHeaderText(rb.getString("invalidappointment"));
            signal.setContentText(isValid);
            Optional<ButtonType> set = signal.showAndWait();
            System.out.println("***** End Appointment Validation *****");
            return false;
        }
    }

    /** Checks to make sure current appointment being set doesn't conflict with customer's other appointments.
     * @param newEnd End of appointment time holder.
     * @param newStart Start of appointment time holder.
     * @return boolean returns true or false depending on if there is a schedule conflict.
     * */
    public boolean validateMeetings(ZonedDateTime newStart, ZonedDateTime newEnd) throws SQLException {
        int meetingNumber = -1;
        int customerID;
        if (MainAppointmentsController.addUpdateFilter == 2) {
            meetingNumber = chosenMeeting.getAppointmentID();
            customerID = chosenMeeting.getCustomerID();
        } else {
            customerID = chosenCust.getCustomerID();
        }
        System.out.println("AppointmentID: " + meetingNumber);
        try {
            PreparedStatement sql = ConnectDB.makeConnection().prepareStatement(
                    "SELECT * FROM appointments WHERE (? BETWEEN start AND end OR ? BETWEEN start AND end OR ? < " +
                            "start AND ? > end) AND (customer_Id = ? AND appointment_ID != ?)");
            sql.setTimestamp(1, Timestamp.valueOf(newStart.toLocalDateTime()));
            sql.setTimestamp(2, Timestamp.valueOf(newEnd.toLocalDateTime()));
            sql.setTimestamp(3, Timestamp.valueOf(newStart.toLocalDateTime()));
            sql.setTimestamp(4, Timestamp.valueOf(newEnd.toLocalDateTime()));
            sql.setInt(5, customerID);
            sql.setInt(6, meetingNumber);
            ResultSet set = sql.executeQuery();
            System.out.println(sql);
            if (set.next()) {
                return true;
            }
        } catch (SQLException sql) {
            System.out.println("SQL contains errors for 'validateMeetings' method.");
            sql.printStackTrace();
        } catch (Exception r) {
            System.out.println("Something other than the SQL has an error.");
            r.printStackTrace();
        }
        return false;
    }

    /** This method inserts newly created appointments into the SQL database. */
    public void insertNewAppSQL() throws Exception {
        System.out.println("**** Start Save Apppointment ****");
        LocalDate userMeetingDay = dateStartPicker.getValue(); //returns start date value without time
        LocalTime userMeetingBeginning = LocalTime.parse(comboBoxStart.getSelectionModel().getSelectedItem(), stageFormatter);
        LocalTime userMeetingEnding = LocalTime.parse(comboBoxEnd.getSelectionModel().getSelectedItem(), stageFormatter);
        LocalDateTime dateOfMeetingStart = LocalDateTime.of(userMeetingDay, userMeetingBeginning);
        LocalDateTime dateOfMeetingEnd = LocalDateTime.of(userMeetingDay, userMeetingEnding);
        System.out.println("localStartDT: " + dateOfMeetingStart);
        System.out.println("localEndDT: " + dateOfMeetingEnd);
        ZonedDateTime universalStart = dateOfMeetingStart.atZone(userTime).withZoneSameInstant(ZoneId.of("UTC"));
        ZonedDateTime universalEnd = dateOfMeetingEnd.atZone(userTime).withZoneSameInstant(ZoneId.of("UTC"));
        System.out.println("universalStart: " + universalStart);
        System.out.println("universalEnd: " + universalEnd);
        Timestamp sqlStartTS = Timestamp.valueOf(universalStart.toLocalDateTime());
        Timestamp sqlEndTS = Timestamp.valueOf(universalEnd.toLocalDateTime());
        System.out.println("sqlStartTime: " + sqlStartTS);
        System.out.println("sqlEndTime: " + sqlEndTS);
        try {
            int meetingNumber = -1;
            PreparedStatement sql = ConnectDB.makeConnection().prepareStatement("INSERT INTO appointments (title, " +
                    "description, location, type, start, end, create_date, created_by, last_update, last_updated_by, " +
                    "customer_id, user_id, contact_id) VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, " +
                    "CURRENT_TIMESTAMP, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            sql.setString(1, titleEntryBox.getText());
            sql.setString(2, descriptionEntryBox.getText());
            sql.setString(3, comboBoxLocation.getSelectionModel().getSelectedItem());
            sql.setString(4, comboBoxType.getSelectionModel().getSelectedItem());
            sql.setTimestamp(5, sqlStartTS);
            sql.setTimestamp(6, sqlEndTS);
            sql.setString(7, User.getUsername());
            sql.setString(8, User.getUsername());
            sql.setInt(9, custListDisplay.getSelectionModel().getSelectedItem().getCustomerID());
            sql.setInt(10, User.getUserID());
            sql.setInt(11, getContactID(comboBoxContact.getValue()));

            chosenCust.setCustomerID(custListDisplay.getSelectionModel().getSelectedItem().getCustomerID());
            System.out.println("sql: " + sql);
            int sets = sql.executeUpdate();
            System.out.println("After SQL execute");
            ResultSet set = sql.getGeneratedKeys();
            if (set.next()) {
                meetingNumber = set.getInt(1);
                System.out.println("Generated AppointmentID: " + meetingNumber);
            }
            System.out.println("Appointment section saved successfully!");
            sql.close();
            set.close();
            emptyDataEntries();
        } catch (SQLException e) {
            System.out.println("Save Appt SQL statement has an error.");
        }
        System.out.println("**** End Save Appointment ****");
    }

    /** This method updates appointment data for current appointments in the SQL database. */
    public void updateCurrentAppSQL() throws Exception {
        System.out.println("**** Start Update Appointment Save ****");
        System.out.println(">>> Begin LocalDate <<<<");
        LocalDate userMeetingDay = dateStartPicker.getValue(); //returns start date value without time
        System.out.println(">>> End LocalDate <<<<");
        LocalTime userMeetingBeginning = LocalTime.parse(comboBoxStart.getSelectionModel().getSelectedItem(), stageFormatter);
        LocalTime userMeetingEnding = LocalTime.parse(comboBoxEnd.getSelectionModel().getSelectedItem(), stageFormatter);
        System.out.println(">>> End LocalTime <<<<");
        LocalDateTime dateOfMeetingStart = LocalDateTime.of(userMeetingDay, userMeetingBeginning);
        LocalDateTime dateOfMeetingEnd = LocalDateTime.of(userMeetingDay, userMeetingEnding);
        System.out.println("localStartDT: " + dateOfMeetingStart);
        System.out.println("localEndDT: " + dateOfMeetingEnd);
        ZonedDateTime universalStart = dateOfMeetingStart.atZone(userTime).withZoneSameInstant(ZoneId.of("UTC"));
        ZonedDateTime universalEnd = dateOfMeetingEnd.atZone(userTime).withZoneSameInstant(ZoneId.of("UTC"));
        System.out.println("universalStart: " + universalStart);
        System.out.println("universalEnd: " + universalEnd);
        Timestamp sqlStartTS = Timestamp.valueOf(universalStart.toLocalDateTime());
        Timestamp sqlEndTS = Timestamp.valueOf(universalEnd.toLocalDateTime());

        //displays values of fields in console used for troubleshooting
        System.out.println("sqlStartTime: " + sqlStartTS);
        System.out.println("sqlEndTime: " + sqlEndTS);
        System.out.println("****************************************");
        System.out.println("CustomerID: " + chosenCust.getCustomerID());
        System.out.println("UserId: " + User.getUserID());
        System.out.println("Title: " + titleEntryBox.getText());
        System.out.println("Description: " + descriptionEntryBox.getText());
        System.out.println("Location: " + comboBoxLocation.getSelectionModel().getSelectedItem());
        System.out.println("Contact: " + comboBoxContact.getSelectionModel().getSelectedItem());
        System.out.println("Type: " + comboBoxType.getSelectionModel().getSelectedItem());
        System.out.println("****************************************");
        try {
            PreparedStatement sql = ConnectDB.makeConnection().prepareStatement("UPDATE appointments SET " +
                    "customer_Id = ?, user_Id = ?, title = ?, description = ?, location = ?, contact_id = ?, " +
                    "type = ?, WHERE appointment_Id = ?");
            System.out.println("CustomerID before check CustomerTable: " + chosenCust.getCustomerID());
            if (chosenCust.getCustomerID() <= 0) {
                chosenCust.setCustomerID(custListDisplay.getSelectionModel().getSelectedItem().getCustomerID());
            }
            System.out.println("CustomerID after check CustomberTable: " + chosenCust.getCustomerID());
            sql.setInt(1, chosenCust.getCustomerID());
            System.out.println("CustomerID: " + chosenCust.getCustomerID());
            sql.setInt(2, User.getUserID());
            sql.setString(3, titleEntryBox.getText());
            sql.setString(4, descriptionEntryBox.getText());
            sql.setString(5, comboBoxLocation.getSelectionModel().getSelectedItem());
            sql.setInt(6, getContactID(comboBoxContact.getValue()));
            sql.setString(7, comboBoxType.getSelectionModel().getSelectedItem());
            sql.setTimestamp(8, sqlStartTS);
            sql.setTimestamp(9, sqlEndTS);
            sql.setString(10, User.getUsername());
            sql.setInt(11, chosenMeeting.getAppointmentID());
            System.out.println("sql SQL: " + sql);
            int set = sql.executeUpdate();
            System.out.println("Appointment UPDATED successfully!");
            emptyDataEntries();
        } catch (SQLException e) {
            System.out.println("Update Appointment method SQL preparedstatement has an error.");
        }
        System.out.println("**** End Update Appointment Save ****");
    }

    /** This method handles the appointment cancel button.
     * When the button is clicked nothing will be saved and text fields will be cleared.
     * @param event In this case a mouse clicking the cancel button.
     * */
    @FXML
    public void buttonToCancel(ActionEvent event) throws IOException {
        Alert signal = new Alert(Alert.AlertType.CONFIRMATION);
        signal.setTitle(rb.getString("confirmationrequired"));
        signal.setHeaderText(rb.getString("confirmcancel"));
        signal.setContentText(rb.getString("areyousure"));
        Optional<ButtonType> set = signal.showAndWait();
        if (set.get() == ButtonType.OK) {
            System.out.println("Returning to Main Appointments Screen.");
            parent = FXMLLoader.load(getClass().getResource("/view/MainAppointments.fxml"));
            setup = (Stage) buttonToSave.getScene().getWindow();
            Scene scene = new Scene(parent);
            setup.setScene(scene);
            setup.show();
        } else {
            System.out.println("Cancel canceled.");
        }
    }

    /** This method handles the appointment save button.
     * When the button is clicked it will save the appointment in the SQL database.
     * @param event In this case a mouse clicking the save button.
     * */
    @FXML
    public void buttonToSaveHandler(ActionEvent event) throws Exception {
        if (meetingValidator()) {
            if (MainAppointmentsController.addUpdateFilter == 1) {
                insertNewAppSQL();
                appUpdateLabel.setText("Add Appointment");
                parent = FXMLLoader.load(getClass().getResource("/view/MainAppointments.fxml"));
                setup = (Stage) buttonToCancel.getScene().getWindow();
                Scene scene = new Scene(parent);
                setup.setScene(scene);
                setup.show();
            } else if (MainAppointmentsController.addUpdateFilter == 2) {
                updateCurrentAppSQL();
                appUpdateLabel.setText("Add Appointment");
                parent = FXMLLoader.load(getClass().getResource("/view/MainAppointments.fxml"));
                setup = (Stage) buttonToCancel.getScene().getWindow();
                Scene scene = new Scene(parent);
                setup.setScene(scene);
                setup.show();
            }
        }
    }
}


