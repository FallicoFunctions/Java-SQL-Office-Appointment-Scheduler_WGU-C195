package controller;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
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
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Appointment;
import model.Customer;
import util.ConnectDB;


/**
 * FXML Controller class.
 * Controls the main appointment screen.
 * @author Nicholas Fallico
 */
public class  MainAppointmentsController implements Initializable {
    @FXML
    public TableColumn colID;
    @FXML
    public TableColumn colDescription;
    @FXML
    public TableColumn colLocation;
    @FXML
    public TableColumn colUserID;
    @FXML
    public RadioButton allSelector;
    @FXML
    public Button newAppButton;
    @FXML
    public Button buttonToUpdate;
    @FXML
    public TableView<Appointment> tableOfAppointments;
    @FXML
    public TableColumn<Appointment, String> colStartTime;
    @FXML
    public TableColumn<Appointment, String> colEndTime;
    @FXML
    public TableColumn<Appointment, String> colTitle;
    @FXML
    public TableColumn<Appointment, String> colType;
    @FXML
    public TableColumn<Appointment, String> colCustomer;
    @FXML
    public TableColumn<Appointment, String> colContact;
    @FXML
    public RadioButton weekSelector;
    @FXML
    public RadioButton monthSelector;

    //Declaring variables
    Parent parent;
    Stage setup;
    boolean shortSort; //Show appointments for this week only
    boolean monthSort; //Show appointments for this month only
    static Appointment reviseApp;
    static int addUpdateFilter; //Changes label between "update" or "add"
    ObservableList<Appointment> appointmentsOL = FXCollections.observableArrayList();
    ResourceBundle rb = ResourceBundle.getBundle("properties.login", Locale.getDefault());

    DateTimeFormatter datetimeDTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    ZoneId localTime = ZoneId.systemDefault();
    ZoneId utcZoneID = ZoneId.of("UTC");

    /** Initializes the controller class.
     * @param url The URL parameter.
     * @param rb The ResourceBundle parameter.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ToggleGroup radioButtonToggleGroup = new ToggleGroup();
        weekSelector.setToggleGroup(radioButtonToggleGroup);
        monthSelector.setToggleGroup(radioButtonToggleGroup);
        allSelector.setToggleGroup(radioButtonToggleGroup);
        weekSelector.setSelected(false);
        monthSelector.setSelected(false);
        allSelector.setSelected(true);
        shortSort = false;
        monthSort = false;

        //Set column data for headers
        colStartTime.setCellValueFactory(new PropertyValueFactory<>("Start"));
        colEndTime.setCellValueFactory(new PropertyValueFactory<>("End"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("Title"));
        colType.setCellValueFactory(new PropertyValueFactory<>("Type"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("CustomerID"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("CreatedBy"));
        colID.setCellValueFactory(new PropertyValueFactory<>("AppointmentID"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("Description"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("Location"));
        colUserID.setCellValueFactory(new PropertyValueFactory<>("UserID"));
        try {
            fillAppListView();
        } catch (SQLException ex) {
            System.out.println("SQL error when 'setAppointmentTable' was called.");
        }
    }

    /** This method fills the GUI view with the list of appointments. */
    public void fillAppListView() throws SQLException {
        System.out.println("**** Start Set Appointment Table ****");
        Statement stmt = ConnectDB.makeConnection().createStatement();
        String sqlStatement = "SELECT appointments.appointment_Id, appointments.customer_Id, appointments.user_Id, " +
                "appointments.title, appointments.description, appointments.location, appointments.contact_id, " +
                "appointments.type, appointments.start, appointments.end, contacts.contact_name, customers.customer_Id, " +
                "customers.customer_Name FROM appointments, customers, contacts WHERE appointments.customer_Id = " +
                "customers.customer_Id AND appointments.Contact_ID = contacts.Contact_ID ORDER BY start";
        try {
            ResultSet set = stmt.executeQuery(sqlStatement);
            System.out.println("PreparedStatement: " + sqlStatement);
            System.out.println("Appointment Table query worked");
            appointmentsOL.clear();
            while (set.next()) {
                //Obtain times for appointments
                String universalStart =set.getString("start").substring(0, 19);
                System.out.println("universalStart: " + universalStart);
                String universalEnd =set.getString("end").substring(0, 19);
                System.out.println("universalEnd: " + universalEnd);
                LocalDateTime universalDateStart = LocalDateTime.parse(universalStart, datetimeDTF);
                System.out.println("universalDateStart: " + universalDateStart);
                LocalDateTime universalDateEnd = LocalDateTime.parse(universalEnd, datetimeDTF);
                System.out.println("universalDateEnd: " + universalDateEnd);
                ZonedDateTime userTimeStart = universalDateStart.atZone(utcZoneID).withZoneSameInstant(localTime);
                System.out.println("userTimeStart: " + userTimeStart);
                ZonedDateTime userTimeEnd = universalDateEnd.atZone(utcZoneID).withZoneSameInstant(localTime);
                System.out.println("userTimeEnd: " + userTimeEnd);
                String userDateStart = userTimeStart.format(datetimeDTF);
                System.out.println("userDateStart: " + userDateStart);
                String userDateEnd = userTimeEnd.format(datetimeDTF);
                System.out.println("userDateEnd: " + userDateEnd);

                //Obtain appointment data from database and assign to the below vars
                String contactNum =set.getString("contact_id");
                System.out.println("contact_id: " + contactNum);
                String description =set.getString("description");
                System.out.println("description: " + description);
                String officePlace =set.getString("location");
                System.out.println("location: " + officePlace);
                int userNum =set.getInt("user_Id");
                System.out.println("userID: " + userNum);
                int appNum =set.getInt("appointment_Id");
                System.out.println("appointmentID: " + appNum);
                int custNum =set.getInt("customer_Id");
                System.out.println("customerID: " + custNum);
                String contactName =set.getString("contact_name");
                System.out.println("contactName: " + contactName);
                String appType =set.getString("type");
                System.out.println("appType: " + appType);
                String title =set.getString("title");
                System.out.println("title: " + title);
                Customer current = new Customer(set.getInt("customer_Id"),set.getString("customer_Name"));
                String customerName = current.getCustomerName();
                System.out.println("Customer Name: " + customerName);

                //load appointment data into the list of appointments for the tableview
                appointmentsOL.add(new Appointment(appNum, custNum, userNum, title, description, officePlace,
                        contactNum, appType, userDateStart, userDateEnd, customerName, contactName));
            }
            //This portion of code sorts the appointment list by week, month, or all at once
            if (shortSort) {
                weekSorter(appointmentsOL);
            } else if (monthSort){
                monthSorter(appointmentsOL);
            } else {
                tableOfAppointments.setItems(appointmentsOL);
            }
        } catch (SQLException sqe) {
            System.out.println("Update Appointment Table SQL error!");
        } catch (Exception e) {
            System.out.println("Something other than SQL has caused an error!");
        }
        System.out.println("**** End Set Appointment Table ****");
    }

    /** This method handles the add new appointment button.
     * @param event When actioned it loads the add appointment fxml file.
     * */
    @FXML
    public void newAppButtonHandler(ActionEvent event) throws IOException {
        addUpdateFilter = 1;
        parent = FXMLLoader.load(getClass().getResource("/view/AddAppointment.fxml"));
        setup = (Stage) newAppButton.getScene().getWindow();
        Scene scene = new Scene(parent);
        setup.setScene(scene);
        setup.show();
    }

    /** This method handles the update appointment button.
     * First checks if an appointment is selected. If not, it alerts as such.
     * @param event When actioned it loads the add appointment fxml file with the pretense of updating a current customer.
     * */
    @FXML
    public void buttonToUpdateHandler(ActionEvent event) throws IOException {
        if (tableOfAppointments.getSelectionModel().getSelectedItem() != null) {
            reviseApp = tableOfAppointments.getSelectionModel().getSelectedItem();
            addUpdateFilter = 2;

            //get reference to the button's stage
            parent = FXMLLoader.load(getClass().getResource("/view/AddAppointment.fxml"));
            setup = (Stage) buttonToUpdate.getScene().getWindow();
            Scene scene = new Scene(parent);
            setup.setScene(scene);
            setup.show();
        } else {
            //Alert that no appointment is selected
            Alert signal = new Alert(Alert.AlertType.CONFIRMATION);
            signal.setTitle(rb.getString("invalidappointmentalert"));
            signal.setHeaderText(rb.getString("nopppointment"));
            signal.setContentText(rb.getString("selectappointment"));
            Optional<ButtonType> result = signal.showAndWait();
            System.out.println("No appointment has been selected to modify.");
        }
    }

    /** This method handles returns reviseApp object that was selected for update.
     * @return Returns reviseApp variable.
     * */
    public static Appointment getReviseApp() {
        return reviseApp;
    }

    /** This method deletes a selected appointment from the database.
     * @param appointment The selected appointment from the appointments table.
     * */
    @FXML
    public void deleteAppointment(Appointment appointment) throws Exception {
        try {
            PreparedStatement sql = ConnectDB.makeConnection().prepareStatement("DELETE appointments.* FROM " +
                    "appointments WHERE appointments.appointment_Id = ? ");
            System.out.println("Delete appointmentID " + appointment.getAppointmentID());
            sql.setInt(1, appointment.getAppointmentID());
            int set = sql.executeUpdate();
        } catch (SQLException e) {
            System.out.println("SQL statement contains an error!");
        }
        fillAppListView();
    }

    /** This method handles the delete button.
     * @param event When clicked it calls the deleteAppointment method.
     * */
    @FXML
    public void deleteHandler(ActionEvent event) throws Exception {
        if (tableOfAppointments.getSelectionModel().getSelectedItem() != null) {
            Appointment current = tableOfAppointments.getSelectionModel().getSelectedItem();
            int appointmentID = current.getAppointmentID();
            System.out.println("AppointmentID : " + appointmentID);

            //Alert asking to confirm deletion
            Alert signal = new Alert(Alert.AlertType.CONFIRMATION);
            signal.setTitle(rb.getString("confirmationrequired"));
            signal.setHeaderText(rb.getString("confirmationdelete"));
            signal.setContentText(rb.getString("confirmdeleteappointment") + current.getAppointmentID() + "?\nAppointment type: " + current.getType());
            Optional<ButtonType> result = signal.showAndWait();

            if (result.get() == ButtonType.OK) {
                System.out.println("Deleting appointment...");
                deleteAppointment(current);
                System.out.println("AppointmentID " + current.getAppointmentID() + " has been deleted!");
                fillAppListView();
            } else {
                System.out.println("DELETE was canceled.");
            }
        } else {
            //Alert that no appointment is selected
            Alert signal = new Alert(Alert.AlertType.CONFIRMATION);
            signal.setTitle(rb.getString("invalidappointmentalert"));
            signal.setHeaderText(rb.getString("nopppointment"));
            signal.setContentText(rb.getString("selectappointment"));
            Optional<ButtonType> result = signal.showAndWait();
            System.out.println("No appointment was selected to delete!");
        }
    }

    /** This method handles the week labeled toggle button.
     * @param event When selected it limits appointments in the table to those within this week.
     * */
    @FXML
    public void weekSelectorHandler(ActionEvent event) throws SQLException, Exception {
        shortSort = true;
        monthSort = false;
        fillAppListView();
    }

    /** This method handles the month labeled toggle button.
     * @param event When selected it limits appointments in the table to those within this month.
     * */
    @FXML
    public void monthSelectorHandler(ActionEvent event) throws Exception {
        shortSort = false;
        monthSort = true;
        fillAppListView();
    }

    /** This method creates an observable list with appointments for this month.
     * @param appointmentsOL The observable list with all appointments.
     * <p>
     * The lambda expression on line 330 filters the appointments observable list. The filter selects all appointments
     * in the current calendar month. It is justified because the code is more efficient, and therefore the program is more efficient.
     * </p>
     * */
    public void monthSorter(ObservableList appointmentsOL) throws SQLException {
        LocalDate current = LocalDate.now();
        FilteredList<Appointment> sortedList = new FilteredList<>(appointmentsOL);
        sortedList.setPredicate(row -> {
            LocalDate selectedTime = LocalDate.parse(row.getStart(), datetimeDTF);
            return selectedTime.getMonth().equals(current.getMonth());
        });
        tableOfAppointments.setItems(sortedList);
    }

    /** This method creates an observablelist with appointments for this week.
     * @param appointmentsOL The observable list with all appointments.
     * <p>
     * The lambda expression on line 349 filters the appointments observable list. The filter selects all appointments
     * in a rolling week starting the day before the current day. It is justified because the code is more efficient,
     * and therefore the program is more efficient.
     * </p>
     * */
    public void weekSorter(ObservableList appointmentsOL) {
        LocalDate current = LocalDate.now();
        LocalDate week = current.plusWeeks(1);
        FilteredList<Appointment> sortedList = new FilteredList<>(appointmentsOL);
        sortedList.setPredicate(row -> {
            LocalDate selectedTime = LocalDate.parse(row.getStart(), datetimeDTF);
            return selectedTime.isAfter(current.minusDays(1)) && selectedTime.isBefore(week);
        });
        tableOfAppointments.setItems(sortedList);
    }

    /** This method handles the back button.
     * @param event When actioned loads the main screen FXML.
     * */
    @FXML
    public void buttonToGoBackHandler(ActionEvent event) throws IOException {
        parent = FXMLLoader.load(getClass().getResource("/view/MainScreen.fxml"));
        setup = (Stage) buttonToUpdate.getScene().getWindow();
        Scene scene = new Scene(parent);
        setup.setScene(scene);
        setup.show();
    }

    /** This method handles the All Appointments toggle button.
     * @param actionEvent When selected it loads all appointments into the appointments table.
     * */
    public void allSelectorHandler(ActionEvent actionEvent) throws SQLException {
        shortSort = false;
        monthSort = false;
        fillAppListView();
    }
}

