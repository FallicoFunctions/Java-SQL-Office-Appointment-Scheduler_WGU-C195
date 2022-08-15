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
    public TableColumn<Appointment, String> contactColumnCustID;
    @FXML
    public TableColumn<Appointment, String> contactColumnTitle;
    @FXML
    public TableColumn<Appointment, String> contactColumnType;
    @FXML
    public TableColumn<Appointment, String> contactColumnStartTime;
    @FXML
    public TableColumn<Appointment, String> contactColumnEndTime;
    @FXML
    public Button goBackButton;
    @FXML
    public TableColumn<Appointment, String> contactColumnID;
    @FXML
    public TableColumn<Appointment, String> contactColumnDescription;
    @FXML
    public ComboBox<String> contactComboBox;
    @FXML
    public ComboBox<String> countryComboBox;
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

    //This will store data for the report screen and display data in a column by row view
    public int dataArray[][] = new int[][]{
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
    
    //Declare variables and observable lists
    ObservableList<Appointment> scheduleOL = FXCollections.observableArrayList();
    ObservableList<Reports> typesByMonthOL = FXCollections.observableArrayList();
    ObservableList<String> contactOptions = FXCollections.observableArrayList();
    ObservableList<Customer> listOfCusts = FXCollections.observableArrayList();
    ObservableList<String> countryOptions = FXCollections.observableArrayList();
    DateTimeFormatter datetimeDTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    Parent parent;
    Stage setup;
    ZoneId userTime = ZoneId.systemDefault();
    ZoneId utcZoneID = ZoneId.of("UTC");

    /** Initializes the controller class.
     * @param url url parameter.
     * @param rb resource bundle parameter.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Fill the customerCountryTable with values
        columnID.setCellValueFactory(new PropertyValueFactory<>("CustomerID"));
        columnName.setCellValueFactory(new PropertyValueFactory<>("CustomerName"));
        columnPhone.setCellValueFactory(new PropertyValueFactory<>("CustomerPhone"));
        columnAddress.setCellValueFactory(new PropertyValueFactory<>("CustomerAddress"));
        columnDivision.setCellValueFactory(new PropertyValueFactory<>("Division"));
        columnPostalCode.setCellValueFactory(new PropertyValueFactory<>("CustomerPostalCode"));

        //Fill the contact schedule tableview
        contactColumnID.setCellValueFactory(new PropertyValueFactory<>("AppointmentID"));
        contactColumnTitle.setCellValueFactory(new PropertyValueFactory<>("Title"));
        contactColumnType.setCellValueFactory(new PropertyValueFactory<>("Type"));
        contactColumnDescription.setCellValueFactory(new PropertyValueFactory<>("Description"));
        contactColumnStartTime.setCellValueFactory(new PropertyValueFactory<>("Start"));
        contactColumnEndTime.setCellValueFactory(new PropertyValueFactory<>("End"));
        contactColumnCustID.setCellValueFactory(new PropertyValueFactory<>("CustomerID"));

        //Fill the type tableview
        colTypeMonth.setCellValueFactory(new PropertyValueFactory<>("Month"));
        colFirstTimePatient.setCellValueFactory(new PropertyValueFactory<>("FirstTimeVisit"));
        colGeneralCheckup.setCellValueFactory(new PropertyValueFactory<>("GeneralCheckup"));
        colBloodWorkVisit.setCellValueFactory(new PropertyValueFactory<>("BloodWorkVisit"));
        colPsychiatricVisit.setCellValueFactory(new PropertyValueFactory<>("PsychiatricVisit"));

        try {
            fillContactCombobox();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            fillTypeTableviewMonth();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            fillCountryComboBox();
        } catch (Exception ex) {
            ex.printStackTrace();
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
    private void fillTypeTableviewMonth() {
        System.out.println("**** Begin Report Type By Month ****");
        String sql = "SELECT * FROM appointments";
        try {
            PreparedStatement pst = ConnectDB.makeConnection().prepareStatement(sql);
            ResultSet set = pst.executeQuery();
            
            System.out.println("Reports By Month query worked");
            typesByMonthOL.clear();
            System.out.println("Entering While Loop");
            while (set.next()) {
                System.out.println("Inside While Loop");
                
                String universalStart = set.getString("start").substring(0, 19);
                System.out.println("UTC Start: " + universalStart);

                String universalEnd = set.getString("end").substring(0, 19);
                System.out.println("UTC End: " + universalEnd);

                LocalDateTime universalDateStart = LocalDateTime.parse(universalStart, datetimeDTF);
                System.out.println("UTC Date Start: " + universalDateStart);
                
                LocalDateTime universalDateEnd = LocalDateTime.parse(universalEnd, datetimeDTF);
                System.out.println("UTC Date End: " + universalDateEnd);

                ZonedDateTime userTimeStart = universalDateStart.atZone(utcZoneID).withZoneSameInstant(userTime);
                System.out.println("userTimeStart: " + userTimeStart);
                
                ZonedDateTime userTimeEnd = universalDateEnd.atZone(utcZoneID).withZoneSameInstant(userTime);
                System.out.println("userTimeEnd: " + userTimeEnd);
                
                String userDateStart = userTimeStart.format(datetimeDTF);
                System.out.println("userDateStart: " + userDateStart);
                
                String userDateEnd = userTimeEnd.format(datetimeDTF);
                System.out.println("userDateEnd: " + userDateEnd);

                String termParse = userDateStart.substring(5, 7);
                int term = Integer.parseInt(termParse);
                System.out.println("Month parsed to Int: " + term);
                term = term - 1;
                String type = set.getString("type");
                System.out.println("month: " + term);
                System.out.println("Type: " + type);

                //Add type values to the tableview
                if (term == 0) {
                    switch (type) {
                        case "First-time Patient" -> dataArray[0][0]++;
                        case "General Checkup" -> dataArray[0][1]++;
                        case "Blood Work Visit" -> dataArray[0][2]++;
                        case "Psychiatric Visit" -> dataArray[0][3]++;
                    }
                }
                if (term == 1) {
                    switch (type) {
                        case "First-time Patient" -> dataArray[1][0]++;
                        case "General Checkup" -> dataArray[1][1]++;
                        case "Blood Work Visit" -> dataArray[1][2]++;
                        case "Psychiatric Visit" -> dataArray[1][3]++;
                    }
                }
                if (term == 2) {
                    switch (type) {
                        case "First-time Patient" -> dataArray[2][0]++;
                        case "General Checkup" -> dataArray[2][1]++;
                        case "Blood Work Visit" -> dataArray[2][2]++;
                        case "Psychiatric Visit" -> dataArray[2][3]++;
                    }
                }
                if (term == 3) {
                    switch (type) {
                        case "First-time Patient" -> dataArray[3][0]++;
                        case "General Checkup" -> dataArray[3][1]++;
                        case "Blood Work Visit" -> dataArray[3][2]++;
                        case "Psychiatric Visit" -> dataArray[3][3]++;
                    }
                }
                if (term == 4) {
                    switch (type) {
                        case "First-time Patient" -> dataArray[4][0]++;
                        case "General Checkup" -> dataArray[4][1]++;
                        case "Blood Work Visit" -> dataArray[4][2]++;
                        case "Psychiatric Visit" -> dataArray[4][3]++;
                    }
                }
                if (term == 5) {
                    switch (type) {
                        case "First-time Patient" -> dataArray[5][0]++;
                        case "General Checkup" -> dataArray[5][1]++;
                        case "Blood Work Visit" -> dataArray[5][2]++;
                        case "Psychiatric Visit" -> dataArray[5][3]++;
                    }
                }
                if (term == 6) {
                    switch (type) {
                        case "First-time Patient" -> dataArray[6][0]++;
                        case "General Checkup" -> dataArray[6][1]++;
                        case "Blood Work Visit" -> dataArray[6][2]++;
                        case "Psychiatric Visit" -> dataArray[6][3]++;
                    }
                }
                if (term == 7) {
                    switch (type) {
                        case "First-time Patient" -> dataArray[7][0]++;
                        case "General Checkup" -> dataArray[7][1]++;
                        case "Blood Work Visit" -> dataArray[7][2]++;
                        case "Psychiatric Visit" -> dataArray[7][3]++;
                    }
                }
                if (term == 8) {
                    switch (type) {
                        case "First-time Patient" -> dataArray[8][0]++;
                        case "General Checkup" -> dataArray[8][1]++;
                        case "Blood Work Visit" -> dataArray[8][2]++;
                        case "Psychiatric Visit" -> dataArray[8][3]++;
                    }
                }
                if (term == 9) {
                    switch (type) {
                        case "First-time Patient" -> dataArray[9][0]++;
                        case "General Checkup" -> dataArray[9][1]++;
                        case "Blood Work Visit" -> dataArray[9][2]++;
                        case "Psychiatric Visit" -> dataArray[9][3]++;
                    }
                }
                if (term == 10) {
                    switch (type) {
                        case "First-time Patient" -> dataArray[10][0]++;
                        case "General Checkup" -> dataArray[10][1]++;
                        case "Blood Work Visit" -> dataArray[10][2]++;
                        case "Psychiatric Visit" -> dataArray[10][3]++;
                    }
                }
                if (term == 11) {
                    switch (type) {
                        case "First-time Patient" -> dataArray[11][0]++;
                        case "General Checkup" -> dataArray[11][1]++;
                        case "Blood Work Visit" -> dataArray[11][2]++;
                        case "Psychiatric Visit" -> dataArray[11][3]++;
                    }
                }
            }
            System.out.println("Exited While Loop");
        } catch (SQLException sqe) {
            System.out.println("Reports By Month has SQL error!");
        } catch (Exception e) {
            System.out.println("Something other than SQL has caused an error!");
        }

        //Load type count into integers for the array
        for (int j = 0; j < 12; j++) {
            int firstTimePatient = dataArray[j][0];
            System.out.println("firstTimePatient: " + firstTimePatient);

            int generalCheckup = dataArray[j][1];
            System.out.println("generalCheckup: " + generalCheckup);

            int bloodWorkVisit = dataArray[j][2];
            System.out.println("bloodWorkVisit: " + bloodWorkVisit);

            int psychiatricVisit = dataArray[j][3];
            System.out.println("psychiatricVisit: " + psychiatricVisit);

            typesByMonthOL.add(new Reports(getTermTrimmed(j), firstTimePatient, generalCheckup, bloodWorkVisit, psychiatricVisit));
        }
        System.out.println("**** End Report Type By Month ****");
    }

    /** This method intakes a month numeral and outputs the month name.
     * @param term Numerical representation of month.
     * */
    private String getTermTrimmed(int term) {
        String trimmedTerm = null;
        if (term == 0) {
            trimmedTerm = "JAN";
        }
        if (term == 1) {
            trimmedTerm = "FEB";
        }
        if (term == 2) {
            trimmedTerm = "MAR";
        }
        if (term == 3) {
            trimmedTerm = "APR";
        }
        if (term == 4) {
            trimmedTerm = "MAY";
        }
        if (term == 5) {
            trimmedTerm = "JUN";
        }
        if (term == 6) {
            trimmedTerm = "JUL";
        }
        if (term == 7) {
            trimmedTerm = "AUG";
        }
        if (term == 8) {
            trimmedTerm = "SEP";
        }
        if (term == 9) {
            trimmedTerm = "OCT";
        }
        if (term == 10) {
            trimmedTerm = "NOV";
        }
        if (term == 11) {
            trimmedTerm = "DEC";
        }
        return trimmedTerm;
    }

    /** This method fills the combobox with names of contacts. */
    public void fillContactCombobox () {
        try {
            Statement stmt = ConnectDB.makeConnection().createStatement();
            String sqlStatement = "SELECT contact_name FROM contacts";
            ResultSet set = stmt.executeQuery(sqlStatement);
            while (set.next()) {
                Appointment app = new Appointment();
                app.setContact(set.getString("contact_name"));
                contactOptions.add(app.getContact());
                contactComboBox.setItems(contactOptions);
            }
            stmt.close();
            set.close();
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

        String sqlStatement = "SELECT contact_Id FROM contacts WHERE contact_name = ?";
        System.out.println(sqlStatement);

        try {
            PreparedStatement pst = ConnectDB.makeConnection().prepareStatement(sqlStatement);
            pst.setString(1, contact_name);
            System.out.println(contact_name);
            ResultSet set = pst.executeQuery();
            while (set.next()) {
                contactID = set.getInt("contact_id");
            }
            System.out.println(contactID);
        } catch (SQLException throwables){
            throwables.printStackTrace();
        }
        return contactID;
    }

    /** This method sets current contact's schedule into table. */
    private void setContactAppointments() {
        System.out.println("**** Begin Report Schedule Table ****");
        try {
            PreparedStatement sql;
            sql = ConnectDB.makeConnection().prepareStatement("SELECT appointments.appointment_Id, " +
                    "appointments.title,  appointments.type, appointments.description, appointments.contact_id, " +
                    "appointments.start, appointments.end, customers.customer_Id FROM appointments, customers " +
                    "WHERE appointments.customer_Id = customers.customer_Id AND appointments.contact_id = ? ORDER BY start");
            sql.setInt(1, getContactID(contactComboBox.getValue()));
            ResultSet set = sql.executeQuery();

            System.out.println("PreparedStatement: " + sql);
            System.out.println("Report Schedule query worked");
            scheduleOL.clear();
            while (set.next()) {
                String universalStart = set.getString("start").substring(0, 19);
                System.out.println(universalStart);

                String universalEnd = set.getString("end").substring(0, 19);
                System.out.println(universalEnd);

                LocalDateTime universalDateStart = LocalDateTime.parse(universalStart, datetimeDTF);
                LocalDateTime universalDateEnd = LocalDateTime.parse(universalEnd, datetimeDTF);

                ZonedDateTime userTimeStart = universalDateStart.atZone(utcZoneID).withZoneSameInstant(userTime);
                ZonedDateTime userTimeEnd = universalDateEnd.atZone(utcZoneID).withZoneSameInstant(userTime);

                String userDateStart = userTimeStart.format(datetimeDTF);
                String userDateEnd = userTimeEnd.format(datetimeDTF);

                int appointmentID = set.getInt("appointment_Id");
                System.out.println(appointmentID);

                String description = set.getString("description");
                System.out.println(description);

                int customerID = set.getInt("customer_Id");
                System.out.println(customerID);

                int contactID = set.getInt("contact_id");
                System.out.println(contactID);

                String title = set.getString("title");
                System.out.println("Title: " + title);

                String type = set.getString("type");
                System.out.println("Type: " + type);

                scheduleOL.add(new Appointment(appointmentID, customerID, title, description, type, userDateStart, userDateEnd));
                System.out.println("Schedule add: " + scheduleOL);
            }
        } catch (SQLException sqle) {
            System.out.println("Report Schedule Table SQL error!");
            System.out.println("Schedule add: " + scheduleOL);
            sqle.printStackTrace();
        } catch (Exception error) {
            System.out.println("Something other than SQL has caused an error!");
        }
        System.out.println("**** End Report Schedule Table ****");
    }

   /** This method handles the contact combobox.
    * @param actionEvent When actioned it calls the setContactAppointments method. */
    @FXML
    public void contactComboBoxHandler(MouseEvent actionEvent) {
        contactComboBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    try {
                        setContactAppointments();
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

        String sqlStatement = "SELECT customers.*, first_level_divisions.division FROM customers, first_level_divisions, countries WHERE customers.Division_ID " +
                "= first_level_divisions.division_id AND first_level_divisions.Country_ID = countries.country_ID AND " +
                "countries.country = ?";
        try {
            PreparedStatement pst = ConnectDB.makeConnection().prepareStatement(sqlStatement);
            pst.setString(1, countryName);
            ResultSet set = pst.executeQuery();
            while (set.next()) {
                Customer current = new Customer();
                current.setCustomerID(set.getInt("customer_Id"));
                current.setCustomerName(set.getString("customer_Name"));
                current.setCustomerAddress(set.getString("address"));
                current.setCustomerPhone(set.getString("phone"));
                current.setCustomerPostalCode(set.getString("postal_code"));
                current.setDivision(set.getString("Division"));
                listOfCusts.addAll(current);
            }
            customerCountryTable.setItems(listOfCusts);
            pst.close();
            set.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        System.out.println("***** End Update Customer Table *****");
    }

    /** This method populates countryComboBox with all available countries. */
    public void fillCountryComboBox() throws SQLException, Exception {
        Statement stmt = ConnectDB.makeConnection().createStatement();
        String sqlStatement = "SELECT country FROM countries";
        ResultSet set = stmt.executeQuery(sqlStatement);
        while (set.next()) {
            Customer current = new Customer();
            current.setCustomerCountry(set.getString("country"));
            countryOptions.add(current.getCustomerCountry());
            countryComboBox.setItems(countryOptions);
        }
        stmt.close();
        set.close();
    }
}

