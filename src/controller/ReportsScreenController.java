package controller;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Appointment;
import model.Customer;
import model.Reports;
import util.ConnectDB;

/** FXML Controller class.
 * Controls the report screen.
 * @author Nicholas Fallico
 */
public class ReportsScreenController implements Initializable {
    @FXML
    public TableColumn<Appointment, String> contactColumnID;
    @FXML
    public TableColumn<Appointment, String> contactColumnDescription;
    @FXML
    public ComboBox<String> contactComboBox;
    @FXML
    public ComboBox<String> countryComboBox;
    @FXML
    public TableColumn columnID;
    @FXML
    public TableColumn columnName;
    @FXML
    public TableColumn columnAddress;
    @FXML
    public TableColumn columnPhone;
    @FXML
    public TableColumn columnPostalCode;
    @FXML
    public TableColumn columnDivision;
    @FXML
    public TableView<Customer> customerCountryTable;
    @FXML
    private TableView<Reports> typesByMonthDisplay;
    @FXML
    private TableColumn<Reports, String> colTypeMonth;
    @FXML
    private TableColumn<Reports, Integer> colFirstTimePatient;
    @FXML
    private TableColumn<Reports, Integer> colGeneralCheckup;
    @FXML
    private TableColumn<Reports, Integer> colBloodWorkVisit;
    @FXML
    private TableColumn<Reports, Integer> colPsychiatricVisit;
    @FXML
    private TableView<Appointment> contactScheduleDisplay;
    @FXML
    private TableColumn<Appointment, String> contactColumnCustID;
    @FXML
    private TableColumn<Appointment, String> contactColumnTitle;
    @FXML
    private TableColumn<Appointment, String> contactColumnType;
    @FXML
    private TableColumn<Appointment, String> contactColumnStartTime;
    @FXML
    private TableColumn<Appointment, String> contactColumnEndTime;
    @FXML
    private Button goBackButton;

    Parent parent;
    Stage setup;

    //creates observablelists used for the 3 available reports
    private final ObservableList<Appointment> scheduleOL = FXCollections.observableArrayList();
    private final ObservableList<Reports> typesByMonthOL = FXCollections.observableArrayList();
    private final ObservableList<String> contactOptions = FXCollections.observableArrayList();
    private final ObservableList<Customer> listOfCusts = FXCollections.observableArrayList();
    private final ObservableList<String> countryOptions = FXCollections.observableArrayList();
    private final DateTimeFormatter datetimeDTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ZoneId localTime = ZoneId.systemDefault();
    private final ZoneId utcZoneID = ZoneId.of("UTC");

    //create array for storing how many of each appointment types are in each month
    private int monthTypes[][] = new int[][]{
            {0, 0, 0, 0},
            {0, 0, 0, 0},
            {0, 0, 0, 0},
            {0, 0, 0, 0},
            {0, 0, 0, 0},
            {0, 0, 0, 0},
            {0, 0, 0, 0},
            {0, 0, 0, 0},
            {0, 0, 0, 0},
            {0, 0, 0, 0},
            {0, 0, 0, 0},
            {0, 0, 0, 0}
    };

    /** Initializes the controller class.
     * @param url url parameter.
     * @param rb resource bundle parameter.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Populate customerCountryTable with values
        columnID.setCellValueFactory(new PropertyValueFactory<>("CustomerID"));
        columnName.setCellValueFactory(new PropertyValueFactory<>("CustomerName"));
        columnPhone.setCellValueFactory(new PropertyValueFactory<>("CustomerPhone"));
        columnAddress.setCellValueFactory(new PropertyValueFactory<>("CustomerAddress"));
        columnDivision.setCellValueFactory(new PropertyValueFactory<>("Division"));
        columnPostalCode.setCellValueFactory(new PropertyValueFactory<>("CustomerPostalCode"));

        //assign cell values to Schedule Report
        contactColumnID.setCellValueFactory(new PropertyValueFactory<>("AppointmentID"));
        contactColumnTitle.setCellValueFactory(new PropertyValueFactory<>("Title"));
        contactColumnType.setCellValueFactory(new PropertyValueFactory<>("Type"));
        contactColumnDescription.setCellValueFactory(new PropertyValueFactory<>("Description"));
        contactColumnStartTime.setCellValueFactory(new PropertyValueFactory<>("Start"));
        contactColumnEndTime.setCellValueFactory(new PropertyValueFactory<>("End"));
        contactColumnCustID.setCellValueFactory(new PropertyValueFactory<>("CustomerID"));

        //assign cell values to Types By Month
        colTypeMonth.setCellValueFactory(new PropertyValueFactory<>("Month"));
        colFirstTimePatient.setCellValueFactory(new PropertyValueFactory<>("NewAccount"));
        colGeneralCheckup.setCellValueFactory(new PropertyValueFactory<>("Consultation"));
        colBloodWorkVisit.setCellValueFactory(new PropertyValueFactory<>("FollowUp"));
        colPsychiatricVisit.setCellValueFactory(new PropertyValueFactory<>("CloseAccount"));

        try {
            fillContactCombobox();
        } catch (Exception ex) {
            Logger.getLogger(ReportsScreenController.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            setReportsTypeByMonthTable();
        } catch (Exception ex) {
            Logger.getLogger(ReportsScreenController.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            fillCountryComboBox();
        } catch (Exception ex) {
            Logger.getLogger(ReportsScreenController.class.getName()).log(Level.SEVERE, null, ex);
        }
        contactScheduleDisplay.setItems(scheduleOL);
        typesByMonthDisplay.setItems(typesByMonthOL);
    }

    /** This method handles the main menu button.
     * When clicked it will take you to the main screen.
     * @param event the clicking of the button.
     * */
    @FXML
    private void goBackButtonHandler(ActionEvent event) throws IOException {
        parent = FXMLLoader.load(getClass().getResource("/view/MainScreen.fxml"));
        setup = (Stage) goBackButton.getScene().getWindow();
        Scene scene = new Scene(parent);
        setup.setScene(scene);
        setup.show();
    }

    /** This method sets the tableview with appointments by type and month. */
    private void setReportsTypeByMonthTable() throws SQLException, Exception {
        System.out.println("**** Begin Report Type By Month ****");
        PreparedStatement ps;
        try {
            ps = ConnectDB.makeConnection().prepareStatement(
                    "SELECT * "
                            + "FROM appointments");

            System.out.println("PreparedStatement: " + ps);
            ResultSet rs = ps.executeQuery();
            System.out.println("Reports By Month query worked");
            typesByMonthOL.clear();
            System.out.println("Entering While Loop");
            while (rs.next()) {
                System.out.println("Inside While Loop");
                //get database start time stored as UTC
                String startUTC = rs.getString("start").substring(0, 19);
                System.out.println("UTC Start: " + startUTC);

                //get database end time stored as UTC
                String endUTC = rs.getString("end").substring(0, 19);
                System.out.println("UTC End: " + endUTC);

                //convert database UTC to LocalDateTime
                LocalDateTime utcStartDT = LocalDateTime.parse(startUTC, datetimeDTF);
                LocalDateTime utcEndDT = LocalDateTime.parse(endUTC, datetimeDTF);

                //convert times UTC zoneId to local zoneId
                ZonedDateTime localZoneStart = utcStartDT.atZone(utcZoneID).withZoneSameInstant(localTime);
                ZonedDateTime localZoneEnd = utcEndDT.atZone(utcZoneID).withZoneSameInstant(localTime);
                System.out.println("localZoneStart: " + localZoneStart);
                System.out.println("localZoneEnd: " + localZoneEnd);

                //convert ZonedDateTime to a string for insertion into AppointmentsTableView
                String localStartDT = localZoneStart.format(datetimeDTF);
                String localEndDT = localZoneEnd.format(datetimeDTF);
                System.out.println("localStartDT: " + localStartDT);
                System.out.println("localEndDT: " + localEndDT);

                String monthParse = localStartDT.substring(5, 7);
                int month = Integer.parseInt(monthParse);
                System.out.println("Month parsed to Int: " + month);
                month = month - 1;
                String type = rs.getString("type");
                System.out.println("Month: " + month);
                System.out.println("Type: " + type);

                //increment array values of each type for each month
                if (month == 0) {
                    switch (type) {
                        case "New Account" -> monthTypes[0][0]++;
                        case "Consultation" -> monthTypes[0][1]++;
                        case "Follow-Up" -> monthTypes[0][2]++;
                        case "Close Account" -> monthTypes[0][3]++;
                    }
                } else if (month == 1) {
                    switch (type) {
                        case "New Account" -> monthTypes[1][0]++;
                        case "Consultation" -> monthTypes[1][1]++;
                        case "Follow-Up" -> monthTypes[1][2]++;
                        case "Close Account" -> monthTypes[1][3]++;
                    }
                } else if (month == 2) {
                    switch (type) {
                        case "New Account" -> monthTypes[2][0]++;
                        case "Consultation" -> monthTypes[2][1]++;
                        case "Follow-Up" -> monthTypes[2][2]++;
                        case "Close Account" -> monthTypes[2][3]++;
                    }
                } else if (month == 3) {
                    switch (type) {
                        case "New Account" -> monthTypes[3][0]++;
                        case "Consultation" -> monthTypes[3][1]++;
                        case "Follow-Up" -> monthTypes[3][2]++;
                        case "Close Account" -> monthTypes[3][3]++;
                    }
                } else if (month == 4) {
                    switch (type) {
                        case "New Account" -> monthTypes[4][0]++;
                        case "Consultation" -> monthTypes[4][1]++;
                        case "Follow-Up" -> monthTypes[4][2]++;
                        case "Close Account" -> monthTypes[4][3]++;
                    }
                } else if (month == 5) {
                    switch (type) {
                        case "New Account" -> monthTypes[5][0]++;
                        case "Consultation" -> monthTypes[5][1]++;
                        case "Follow-Up" -> monthTypes[5][2]++;
                        case "Close Account" -> monthTypes[5][3]++;
                    }
                } else if (month == 6) {
                    switch (type) {
                        case "New Account" -> monthTypes[6][0]++;
                        case "Consultation" -> monthTypes[6][1]++;
                        case "Follow-Up" -> monthTypes[6][2]++;
                        case "Close Account" -> monthTypes[6][3]++;
                    }
                } else if (month == 7) {
                    switch (type) {
                        case "New Account" -> monthTypes[7][0]++;
                        case "Consultation" -> monthTypes[7][1]++;
                        case "Follow-Up" -> monthTypes[7][2]++;
                        case "Close Account" -> monthTypes[7][3]++;
                    }
                } else if (month == 8) {
                    switch (type) {
                        case "New Account" -> monthTypes[8][0]++;
                        case "Consultation" -> monthTypes[8][1]++;
                        case "Follow-Up" -> monthTypes[8][2]++;
                        case "Close Account" -> monthTypes[8][3]++;
                    }
                } else if (month == 9) {
                    switch (type) {
                        case "New Account" -> monthTypes[9][0]++;
                        case "Consultation" -> monthTypes[9][1]++;
                        case "Follow-Up" -> monthTypes[9][2]++;
                        case "Close Account" -> monthTypes[9][3]++;
                    }
                } else if (month == 10) {
                    switch (type) {
                        case "New Account" -> monthTypes[10][0]++;
                        case "Consultation" -> monthTypes[10][1]++;
                        case "Follow-Up" -> monthTypes[10][2]++;
                        case "Close Account" -> monthTypes[10][3]++;
                    }
                } else if (month == 11) {
                    switch (type) {
                        case "New Account" -> monthTypes[11][0]++;
                        case "Consultation" -> monthTypes[11][1]++;
                        case "Follow-Up" -> monthTypes[11][2]++;
                        case "Close Account" -> monthTypes[11][3]++;
                    }
                }
            }
            System.out.println("Exited While Loop");
        } catch (SQLException sqe) {
            System.out.println("Reports By Month has SQL error!");
        } catch (Exception e) {
            System.out.println("Something other than SQL has caused an error!");
        }
        for (int i = 0; i < 12; i++) {
            //assign variables for insertion into typesByMonthOL
            int newAccount = monthTypes[i][0];
            int consultation = monthTypes[i][1];
            int followUp = monthTypes[i][2];
            int closeAccount = monthTypes[i][3];

            //prints variable contents to terminal for troubleshooting
            System.out.println("newAccount: " + newAccount);
            System.out.println("consultation: " + consultation);
            System.out.println("followUp: " + followUp);
            System.out.println("closeAccount: " + closeAccount);

            typesByMonthOL.add(new Reports(getAbbreviatedMonth(i), newAccount, consultation, followUp, closeAccount));
        }
        System.out.println("**** End Report Type By Month ****");
    }

    /** This method converts two digit month code into abbreviated month string.
     * @param month Numerical representation of month.
     * */
    private String getAbbreviatedMonth(int month) {
        String abbreviatedMonth = null;
        if (month == 0) {
            abbreviatedMonth = "JAN";
        }
        if (month == 1) {
            abbreviatedMonth = "FEB";
        }
        if (month == 2) {
            abbreviatedMonth = "MAR";
        }
        if (month == 3) {
            abbreviatedMonth = "APR";
        }
        if (month == 4) {
            abbreviatedMonth = "MAY";
        }
        if (month == 5) {
            abbreviatedMonth = "JUN";
        }
        if (month == 6) {
            abbreviatedMonth = "JUL";
        }
        if (month == 7) {
            abbreviatedMonth = "AUG";
        }
        if (month == 8) {
            abbreviatedMonth = "SEP";
        }
        if (month == 9) {
            abbreviatedMonth = "OCT";
        }
        if (month == 10) {
            abbreviatedMonth = "NOV";
        }
        if (month == 11) {
            abbreviatedMonth = "DEC";
        }
        return abbreviatedMonth;
    }

    /** This method fills the combobox with names of contacts. */
    public void fillContactCombobox () {
        try {
            //create statement object
            Statement stmt = ConnectDB.makeConnection().createStatement();

            //Write SQL statement (columns from two tables)
            String sqlStatement = "SELECT contact_name FROM contacts";

            //execute statement and create resultset object
            ResultSet result = stmt.executeQuery(sqlStatement);

            //get all records from resultset obj
            while (result.next()) {
                Appointment app = new Appointment();
                app.setContact(result.getString("contact_name"));
                contactOptions.add(app.getContact());
                contactComboBox.setItems(contactOptions);
            }
            stmt.close();
            result.close();
        } catch (SQLException throwables){
            throwables.printStackTrace();
        }
    }

    /** Gets contact's ID based on their name.
     * @param contact_name The contact's number.
     * @return Returns the contact's ID.
     * */
    private int getContactID(String contact_name) {
        int contactID = -1;
        //Write SQL statement to select contact ID from Contact name
        String sqlStatement = "SELECT contact_Id FROM contacts WHERE contact_name = ?";
        System.out.println(sqlStatement);

        try {
            //create statement object
            PreparedStatement pst = ConnectDB.makeConnection().prepareStatement(sqlStatement);
            pst.setString(1, contact_name);
            System.out.println(contact_name);

            //execute statement and create resultset object
            ResultSet result = pst.executeQuery();

            //get all records from resultset obj
            while (result.next()) {
                contactID = result.getInt("contact_id");
            }
            System.out.println(contactID);
        } catch (SQLException throwables){
            throwables.printStackTrace();
        }
        return contactID;
    }

    /** This method sets current contact's schedule into table. */
    private void setReportsScheduleTable() {
        System.out.println("**** Begin Report Schedule Table ****");
        PreparedStatement ps;
        try {
            ps = ConnectDB.makeConnection().prepareStatement(
                    "SELECT appointments.appointment_Id, appointments.title,  appointments.type, appointments.description, appointments.contact_id, " +
                            "appointments.start, appointments.end, customers.customer_Id FROM appointments, customers " +
                            "WHERE appointments.customer_Id = customers.customer_Id AND appointments.contact_id = ? ORDER BY start");

            ps.setInt(1, getContactID(contactComboBox.getValue()));

            System.out.println("PreparedStatement: " + ps);
            ResultSet rs = ps.executeQuery();
            System.out.println("Report Schedule query worked");
            scheduleOL.clear();

            while (rs.next()) {
                int appointmentID = rs.getInt("appointment_Id");
                System.out.println(appointmentID);
                String description = rs.getString("description");
                System.out.println(description);
                int customerID = rs.getInt("customer_Id");
                System.out.println(customerID);
                int contactID = rs.getInt("contact_id");
                System.out.println(contactID);

                //get database start time stored as UTC
                String startUTC = rs.getString("start").substring(0, 19);
                System.out.println(startUTC);

                //get database end time stored as UTC
                String endUTC = rs.getString("end").substring(0, 19);
                System.out.println(endUTC);

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
                System.out.println("Title: " + title);

                //get type from appointment
                String type = rs.getString("type");
                System.out.println("Type: " + type);

                scheduleOL.add(new Appointment(appointmentID, customerID, title, description, type, localStartDT, localEndDT));
                System.out.println("Schedule add: " + scheduleOL);
            }

            //filter appointments by week or month
        } catch (SQLException sqe) {
            System.out.println("Report Schedule Table SQL error!");
            System.out.println("Schedule add: " + scheduleOL);
            sqe.printStackTrace();
        } catch (Exception e) {
            System.out.println("Something other than SQL has caused an error!");
        }
        System.out.println("**** End Report Schedule Table ****");
    }

   /** This method handles the contact combobox.
    * @param actionEvent When actioned it calls the setReportsScheduleTable method. */
    @FXML
    public void contactComboBoxHandler(MouseEvent actionEvent) {
        contactComboBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    try {
                        setReportsScheduleTable();
                        System.out.println(contactComboBox.getValue());
                    } catch (Exception ex) {
                        System.out.println("Customer Listener had an error!");
                        ex.printStackTrace();
                    }
                });
    }

    /** This method handles the country combobox.
     * @param actionEvent When actioned it calls the updateCustomerCountryTable method.
     * <p>
     * There is a lambda listener expression on line 533. It is justified because it listens for the mouse click in the
     * country combobox. Then it populates the customer table with customers from that country. It is more efficient
     * to use a lambda for this task.
     * </p>
     * */
    @FXML
    public void countryComboBoxHandler(MouseEvent actionEvent) {
        countryComboBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    try {
                        updateCustomerCountryTable(countryComboBox.getValue());
                    } catch (Exception ex) {
                        System.out.println("Customer Listener had an error!");
                        ex.printStackTrace();
                    }
                });
    }

    /** This method loads customers and their info into the table.
     * This loads customers based on country selected.
     * @param countryName The country name that is passed through.
     * <P>
     * This table is for the third report that was up to my discretion. I chose to create a report that displays customers
     * based on their country. This method is called in the countryComboBoxHandler method. When a user selects a country
     * from the combobox, this method is called with the specified country. It will load customers from the selected
     * country into the tableview.
     * </P>
     * */
    public void updateCustomerCountryTable(String countryName) throws SQLException {
        System.out.println("***** Begin Update Customer Table *****");
        listOfCusts.clear();

        //Write SQL statement (columns from two tables)
        String sqlStatement = "SELECT customers.*, first_level_divisions.division FROM customers, first_level_divisions, countries WHERE customers.Division_ID " +
                "= first_level_divisions.division_id AND first_level_divisions.Country_ID = countries.country_ID AND " +
                "countries.country = ?";

        try {
            //create statement object
            PreparedStatement pst = ConnectDB.makeConnection().prepareStatement(sqlStatement);
            pst.setString(1, countryName);

            //execute statement and create resultset object
            ResultSet result = pst.executeQuery();

            //get all records from resultset object
            while (result.next()) {
                Customer cust = new Customer();
                cust.setCustomerID(result.getInt("customer_Id"));
                cust.setCustomerName(result.getString("customer_Name"));
                cust.setCustomerAddress(result.getString("address"));
                cust.setCustomerPhone(result.getString("phone"));
                cust.setCustomerPostalCode(result.getString("postal_code"));
                cust.setDivision(result.getString("Division"));
                listOfCusts.addAll(cust);
            }
            customerCountryTable.setItems(listOfCusts);
            pst.close();
            result.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        System.out.println("***** End Update Customer Table *****");
    }

    /** This method populates countryComboBox with all available countries. */
    public void fillCountryComboBox() throws SQLException, Exception {
        //create statement object
        Statement stmt = ConnectDB.makeConnection().createStatement();

        //Write SQL statement (columns from two tables)
        String sqlStatement = "SELECT country FROM countries";

        //execute statement and create resultset object
        ResultSet result = stmt.executeQuery(sqlStatement);

        //get all records from resultset obj
        while (result.next()) {
            Customer cust = new Customer();
            cust.setCustomerCountry(result.getString("country"));
            countryOptions.add(cust.getCustomerCountry());
            countryComboBox.setItems(countryOptions);
        }
        stmt.close();
        result.close();
    }
}

