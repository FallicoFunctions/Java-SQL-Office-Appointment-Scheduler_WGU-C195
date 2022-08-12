package controller;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    private Button newAppButton;
    @FXML
    private Button updateButton;
    @FXML
    private TableView<Appointment> tableOfAppointments;
    @FXML
    private TableColumn<Appointment, String> colStartTime;
    @FXML
    private TableColumn<Appointment, String> colEndTime;
    @FXML
    private TableColumn<Appointment, String> colTitle;
    @FXML
    private TableColumn<Appointment, String> colType;
    @FXML
    private TableColumn<Appointment, String> colCustomer;
    @FXML
    private TableColumn<Appointment, String> colContact;
    @FXML
    private RadioButton weekSelector;
    @FXML
    private RadioButton monthSelector;

    private boolean isWeekly;
    private boolean isMonthly;
    private static Appointment updateAppointment;
    public static int addUpdateFilter; //used to identify Add/Update label on AppointmentAddController

    Parent parent;
    Stage setup;

    ObservableList<Appointment> appointmentsOL = FXCollections.observableArrayList();
    ResourceBundle rb = ResourceBundle.getBundle("properties.login", Locale.getDefault());

    //private User currentUser;
    private final DateTimeFormatter datetimeDTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ZoneId localTime = ZoneId.systemDefault();
    private final ZoneId utcZoneID = ZoneId.of("UTC");

    /** Initializes the controller class.
     * @param url The URL parameter.
     * @param rb The ResourceBundle parameter.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Populate CustomerTable with values
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

        //sets togglegroup
        ToggleGroup radioButtonToggleGroup = new ToggleGroup();
        weekSelector.setToggleGroup(radioButtonToggleGroup);
        monthSelector.setToggleGroup(radioButtonToggleGroup);
        allSelector.setToggleGroup(radioButtonToggleGroup);
        weekSelector.setSelected(false);
        monthSelector.setSelected(false);
        allSelector.setSelected(true);

        isWeekly = false;
        isMonthly = false;

        try {
            setAppointmentsTable();
        } catch (SQLException ex) {
            System.out.println("SQL error when 'setAppointmentTable' was called.");
        }
    }

    /** This method handles returns updateAppointment object that was selected for update.
     * @return Returns updateAppointment variable.
     * */
    public static Appointment getUpdateAppointment() {
        return updateAppointment;
    }

    /** This method populates table view with appointments and applies filter. */
    public void setAppointmentsTable() throws SQLException {
        System.out.println("**** Start Set Appointment Table ****");
        PreparedStatement ps;
        try {
            ps = ConnectDB.makeConnection().prepareStatement("SELECT appointments.appointment_Id, appointments.customer_Id, " +
                    "appointments.user_Id, appointments.title, appointments.description, appointments.location, appointments.contact_id, " +
                    "appointments.type, appointments.start, appointments.end, contacts.contact_name, customers.customer_Id, " +
                    "customers.customer_Name FROM appointments, customers, contacts WHERE appointments.customer_Id = customers.customer_Id AND appointments.Contact_ID = contacts.Contact_ID " +
                    "ORDER BY start");

            System.out.println("PreparedStament: " + ps);
            ResultSet rs = ps.executeQuery();
            System.out.println("Appointment Table query worked");
            appointmentsOL.clear();

            while (rs.next()) {
                //assigns variables with data from db for insertion into appointments observablelist
                int appointmentID = rs.getInt("appointment_Id");
                int customerID = rs.getInt("customer_Id");
                int userID = rs.getInt("user_Id");
                String description = rs.getString("description");
                String location = rs.getString("location");
                String contact = rs.getString("contact_id");

                //get database start time stored as UTC
                String startUTC = rs.getString("start").substring(0, 19);

                //get database end time stored as UTC
                String endUTC = rs.getString("end").substring(0, 19);

                //convert database UTC to LocalDateTime
                LocalDateTime utcStartDT = LocalDateTime.parse(startUTC, datetimeDTF);
                LocalDateTime utcEndDT = LocalDateTime.parse(endUTC, datetimeDTF);

                //convert times UTC zoneId to local zoneId
                ZonedDateTime localZoneStart = utcStartDT.atZone(utcZoneID).withZoneSameInstant(localTime);
                ZonedDateTime localZoneEnd = utcEndDT.atZone(utcZoneID).withZoneSameInstant(localTime);

                //convert ZonedDateTime to a string for insertion into AppointmentsTableView
                String localStartDT = localZoneStart.format(datetimeDTF);
                String localEndDT = localZoneEnd.format(datetimeDTF);

                //get title from appointment
                String title = rs.getString("title");

                //get type from appointment
                String type = rs.getString("type");

                //put Customer data into Customer object
                Customer customer = new Customer(rs.getInt("customer_Id"), rs.getString("customer_Name"));
                String customerName = customer.getCustomerName();

                //System.out.println("Customer Name: " + customerName);
                String user = rs.getString("contact_name");

                //insert appointments into observablelist for tableOfAppointments if userName = createdBy
                appointmentsOL.add(new Appointment(appointmentID, customerID, userID, title, description, location, contact, type, localStartDT, localEndDT, customerName, user));
            }

            //filter appointments by week or month
            if (isWeekly) {
                filterAppointmentsByWeek(appointmentsOL);
            } else if (isMonthly){
                filterAppointmentsByMonth(appointmentsOL);
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
    private void newAppButtonHandler(ActionEvent event) throws IOException {
        addUpdateFilter = 1;
        parent = FXMLLoader.load(getClass().getResource("/view/AddAppointment.fxml"));
        setup = (Stage) newAppButton.getScene().getWindow();
        Scene scene = new Scene(parent);
        setup.setScene(scene);
        setup.show();
    }

    /** This method handles the update appointment button.
     * @param event When actioned it loads the add appointment fxml file with the pretense of updating a current customer.
     * */
    @FXML
    private void updateButtonHandler(ActionEvent event) throws IOException {
        //Check that a part has been selected
        if (tableOfAppointments.getSelectionModel().getSelectedItem() != null) {
            updateAppointment = tableOfAppointments.getSelectionModel().getSelectedItem();
            System.out.println("AppointmentID: " + updateAppointment.getAppointmentID());
            int updateAppointmentIndex = appointmentsOL.indexOf(updateAppointment);
            addUpdateFilter = 2;

            //get reference to the button's stage
            parent = FXMLLoader.load(getClass().getResource("/view/AddAppointment.fxml"));
            setup = (Stage) updateButton.getScene().getWindow();
            Scene scene = new Scene(parent);
            setup.setScene(scene);
            setup.show();
        } else {
            System.out.println("No appointment has been selected to modify.");
        }
    }

    /** This method handles deletes a selected appointment.
     * @param appointment The selected appointment from the appointments table.
     * */
    @FXML
    private void deleteAppointment(Appointment appointment) throws Exception {
        Appointment appt = appointment;
        try {
            PreparedStatement ps = ConnectDB.makeConnection().prepareStatement("DELETE appointments.* FROM appointments WHERE appointments.appointment_Id = ? ");
            System.out.println("Delete appointmentID " + appt.getAppointmentID());
            ps.setInt(1, appt.getAppointmentID());
            int result = ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("SQL statement contains an error!");
        }
        setAppointmentsTable();
    }

    /** This method handles the delete button.
     * @param event When clicked it calls the deleteAppointment method.
     * */
    @FXML
    private void deleteHandler(ActionEvent event) throws Exception {
        if (tableOfAppointments.getSelectionModel().getSelectedItem() != null) {

            Appointment appt = tableOfAppointments.getSelectionModel().getSelectedItem();
            int appointmentID = appt.getAppointmentID();
            System.out.println("AppointmentID : " + appointmentID);

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(rb.getString("confirmationrequired"));
            alert.setHeaderText(rb.getString("confirmationdelete"));
            alert.setContentText(rb.getString("confirmdeleteappointment") + appt.getAppointmentID() + "?\nAppointment type: " + appt.getType());
            Optional<ButtonType> result = alert.showAndWait();

            if (result.get() == ButtonType.OK) {
                System.out.println("Deleting appointment...");
                deleteAppointment(appt);
                System.out.println("AppointmentID " + appt.getAppointmentID() + " has been deleted!");
                setAppointmentsTable();
            } else {
                System.out.println("DELETE was canceled.");
            }
        } else {
            System.out.println("No appointment was selected to delete!");
        }
    }

    /** This method handles the week labeled toggle button.
     * @param event When selected it limits appointments in the table to those within this week.
     * */
    @FXML
    private void weekSelectorHandler(ActionEvent event) throws SQLException, Exception {
        isWeekly = true;
        isMonthly = false;
        setAppointmentsTable();
    }

    /** This method handles the month labeled toggle button.
     * @param event When selected it limits appointments in the table to those within this month.
     * */
    @FXML
    private void monthSelectorHandler(ActionEvent event) throws SQLException, Exception {
        isWeekly = false;
        isMonthly = true;
        setAppointmentsTable();
    }

    /** This method creates an observable list with appointments for this month.
     * @param appointmentsOL The observable list with all appointments.
     * <p>
     * The lambda expression in this method filters the appointments observable list. The filter selects all appointments
     * in the current calendar month. It is justified because the code is more efficient, and therefore the program is more efficient.
     * </p>
     * */
    public void filterAppointmentsByMonth(ObservableList appointmentsOL) throws SQLException {
        //filter appointments for month
        LocalDate now = LocalDate.now();

        //lambda expression used to efficiently filter appointments by month
        FilteredList<Appointment> filteredData = new FilteredList<>(appointmentsOL);
        filteredData.setPredicate(row -> {
            LocalDate rowDate = LocalDate.parse(row.getStart(), datetimeDTF);
            return rowDate.getMonth().equals(now.getMonth());
        });
        tableOfAppointments.setItems(filteredData);
    }

    /** This method creates an observablelist with appointments for this week.
     * @param appointmentsOL The observable list with all appointments.
     * <p>
     * The lambda expression in this method filters the appointments observable list. The filter selects all appointments
     * in a rolling week starting the day before the current day. It is justified because the code is more efficient,
     * and therefore the program is more efficient.
     * </p>
     * */
    public void filterAppointmentsByWeek(ObservableList appointmentsOL) {
        //filter appointments for week
        LocalDate now = LocalDate.now();
        LocalDate nowPlus1Week = now.plusWeeks(1);

        //lambda expression used to efficiently filter appointments by week
        FilteredList<Appointment> filteredData = new FilteredList<>(appointmentsOL);
        filteredData.setPredicate(row -> {
            LocalDate rowDate = LocalDate.parse(row.getStart(), datetimeDTF);
            return rowDate.isAfter(now.minusDays(1)) && rowDate.isBefore(nowPlus1Week);
        });
        tableOfAppointments.setItems(filteredData);
    }

    /** This method handles the back button.
     * @param event When actioned loads the main screen FXML.
     * */
    @FXML
    private void AppointmentsBackButtonHandler(ActionEvent event) throws IOException {
        parent = FXMLLoader.load(getClass().getResource("/view/MainScreen.fxml"));
        setup = (Stage) updateButton.getScene().getWindow();
        Scene scene = new Scene(parent);
        setup.setScene(scene);
        setup.show();
    }

    /** This method handles the All Appointments toggle button.
     * @param actionEvent When selected it loads all appointments into the appointments table.
     * */
    public void allSelectorHandler(ActionEvent actionEvent) throws SQLException {
        isWeekly = false;
        isMonthly = false;
        setAppointmentsTable();
    }
}

