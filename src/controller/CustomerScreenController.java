package controller;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Customer;
import model.User;
import util.ConnectDB;

/** FXML Controller class.
 * Controls the customer screen.
 * @author Nicholas Fallico
 */
public class CustomerScreenController implements Initializable {
    @FXML
    public TableColumn columnAddress;
    @FXML
    public TableColumn columnPostalCode;
    @FXML
    public TableColumn columnDivision;
    @FXML
    private Label addUpdateLabel;
    @FXML
    private TableView<Customer> CustomerTable;
    @FXML
    private TableColumn<Customer, Integer> columnID;
    @FXML
    private TableColumn<Customer, String> columnName;
    @FXML
    private TableColumn<Customer, String> columnPhone;
    @FXML
    private TextField custIDEntryBox;
    @FXML
    private ComboBox<String> comboBoxFirstLevel;
    @FXML
    private ComboBox<String> comboBoxCounty;
    @FXML
    private Button buttonToSave;
    @FXML
    private Button buttonToCancel;
    @FXML
    private Button CustomerDeleteButton;
    @FXML
    private TextField custNameEntryBox;
    @FXML
    private TextField custAddressEntryBox;
    @FXML
    private TextField zipCodeEntryBox;
    @FXML
    private TextField phoneEntryBox;
    @FXML
    private Button buttonToGoBack;

    Parent parent;
    Stage setup;

    //create ObservableLists
    ObservableList<Customer> listOfCusts = FXCollections.observableArrayList();
    ObservableList<String> firstLevelOptions = FXCollections.observableArrayList();
    ObservableList<String> countryOptions = FXCollections.observableArrayList();

    private boolean customerUpdate = false; //used to determine whether to UPDATE customer in the database
    private boolean customerAdd = false; //used to determine whether to INSERT customer in the database

    ResourceBundle rb = ResourceBundle.getBundle("properties.login", Locale.getDefault());

    /** Initializes the controller class.
     * @param url The URL parameter.
     * @param rb The ResourceBundle parameter.
     * <p>
     * There is a lambda listener expression on line 132. It is justified because it listens for the mouse click in the
     * customer table. Then it populates the customer form with the selected customer's info. It is more efficient
     * to use a lambda for this task.
     * </p>
     * */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Populate CustomerTable with values
        columnID.setCellValueFactory(new PropertyValueFactory<>("CustomerID"));
        columnName.setCellValueFactory(new PropertyValueFactory<>("CustomerName"));
        columnPhone.setCellValueFactory(new PropertyValueFactory<>("CustomerPhone"));
        columnAddress.setCellValueFactory(new PropertyValueFactory<>("CustomerAddress"));
        columnDivision.setCellValueFactory(new PropertyValueFactory<>("Division"));
        columnPostalCode.setCellValueFactory(new PropertyValueFactory<>("CustomerPostalCode"));

        custIDEntryBox.setText("Auto Generated");

        //disable input for CustomerID since its auto-generated
        disableCustomerFields();

        try {
            System.out.println("Current userID: " + User.getUserID());
            System.out.println("Current Username: " + User.getUsername());
            updateCustomerTable();
            try {
                fillCountryComboBox();
            } catch (Exception ex) {
                ex.printStackTrace();
                Logger.getLogger(CustomerScreenController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            Logger.getLogger(CustomerScreenController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Listen for mouse click on item in Customer Table
        CustomerTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    try {
                        customerListener(newValue);
                    } catch (SQLException ex) {
                        System.out.println("Customer Listener had an error!");
                        ex.printStackTrace();;
                    }
                });
    }

    /** This method loads customers and their info into the table. */
    public void updateCustomerTable() throws SQLException {
        System.out.println("***** Begin Update Customer Table *****");
        listOfCusts.clear();
        //create statement object
        Statement stmt = ConnectDB.conn.createStatement();

        //Write SQL statement (columns from two tables)
        String sqlStatement = "SELECT customer_Id, customer_Name, phone, address, Postal_Code, first_level_divisions.Division, first_level_divisions.Division_ID, Country_ID FROM customers, first_level_divisions WHERE customers.Division_ID = first_level_divisions.Division_ID ORDER BY customers.customer_Name";

        //execute statement and create resultset object
        ResultSet result = stmt.executeQuery(sqlStatement);

        //get all records from resultset object
        while (result.next()) {
            Customer cust = new Customer();
            cust.setCustomerID(result.getInt("customer_Id"));
            cust.setCustomerName(result.getString("customer_Name"));
            cust.setCustomerAddress(result.getString("address"));
            cust.setCustomerPhone(result.getString("phone"));
            cust.setCustomerPostalCode(result.getString("postal_code"));
            cust.setDivision(result.getString("division"));
            listOfCusts.addAll(cust);
        }
        CustomerTable.setItems(listOfCusts);
        System.out.println("***** End Update Customer Table *****");
    }

    /** This method clears all comboboxes and text fields. */
    public void clearTextFields() {
        addUpdateLabel.setText("");
        custIDEntryBox.setText("");
        custNameEntryBox.setText("");
        custAddressEntryBox.setText("");
        comboBoxFirstLevel.setValue("");
        comboBoxCounty.setValue("");
        zipCodeEntryBox.setText("");
        phoneEntryBox.setText("");
    }

    /** This method fills the first level box based on country selected.
     * @param countryName Name of country selected and passed through to obtain first level divisions.
     * */
    public void fillFirstLevelBox(String countryName) {
        //Write SQL statement
        System.out.println(countryName);
        comboBoxFirstLevel.getItems().clear();

        String sqlStatement = "SELECT first_level_divisions.* FROM first_level_divisions, countries WHERE first_level_divisions.Country_ID = countries.country_ID AND countries.country = ?";

        try {
            //create statement object
            PreparedStatement pst = ConnectDB.makeConnection().prepareStatement(sqlStatement);
            pst.setString(1, countryName);

            //execute statement and create resultset object
            ResultSet result = pst.executeQuery();

            //get all records from resultset obj
            while (result.next()) {
                Customer cust = new Customer();
                cust.setDivision(result.getString("division"));
                firstLevelOptions.add(cust.getDivision());
                comboBoxFirstLevel.setItems(firstLevelOptions);
            }
            pst.close();
            result.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /** This method populates CountryComboBox with all available countries. */
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
            comboBoxCounty.setItems(countryOptions);
        }
        stmt.close();
        result.close();
    }

    /** This method calls the firstLevelBox method when this handler is activated.
     * @param event When actioned it calls the fillFirstLevelBox method.
     * <p>
     * There is a lambda listener expression on line 247. It is justified because it listens for the mouse click in the
     * country combobox. Then it populates the first level box with the selected country's division. It is more efficient
     * to use a lambda for this task.
     * </p>
     * */
    @FXML
    private void comboBoxCountyHandler(ActionEvent event) {
        comboBoxCounty.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    try {
                        fillFirstLevelBox(comboBoxCounty.getValue());
                    } catch (Exception ex) {
                        System.out.println("Customer Listener had an error!");
                        ex.printStackTrace();
                    }
                });
    }

    /** This method calls other methods to save customer info.
     * saveCustomer method if new customer.
     * updateCustomer method if existing customer.
     * @param event When actioned it calls either of the mentioned methods.
     * */
    @FXML
    private void buttonToSaveHandler(ActionEvent event) throws Exception {
        System.out.println("CustomerAdd: " + customerAdd);
        System.out.println("CustomerUpdate: " + customerUpdate);
        if (custNameEntryBox.getText() != null && customerAdd || customerUpdate) {
            if (validCustomer()) {
                if (customerAdd) {
                    saveCustomer();
                    clearTextFields();
                    updateCustomerTable();
                } else if (customerUpdate) {
                    updateCustomer();
                    clearTextFields();
                    updateCustomerTable();
                }
            }
        } else {
            System.out.println("No customer selected to save!");
        }
    }

    /** This method saves a new customer. */
    private void saveCustomer() throws Exception {
        System.out.println("***** Begin Save Customer *****");
        try {
            //Insert new customer into DB
            PreparedStatement psc = ConnectDB.makeConnection().prepareStatement("INSERT INTO customers (customer_Name, address, postal_code, phone, create_Date, created_By, last_Update, last_Updated_By, division_Id) "
                    + "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?, ?)");

            psc.setString(1, custNameEntryBox.getText());
            psc.setString(2, custAddressEntryBox.getText());
            psc.setString(3, zipCodeEntryBox.getText());
            psc.setString(4, phoneEntryBox.getText());
            psc.setString(5, User.getUsername());
            psc.setString(6, User.getUsername());
            psc.setInt(7, getDivisionID(comboBoxFirstLevel.getValue()));
            int results = psc.executeUpdate();

            psc.close();
        } catch (SQLException e) {
            System.out.println("SQL statement has an error!");
            e.printStackTrace();
        }
        clearTextFields();
        disableCustomerFields();
        updateCustomerTable();
        customerAdd = false;
        customerUpdate = false;

        System.out.println("***** End Save Customer *****");
    }

    /** This method deletes the selected customer in customer table.
     * @param customer Customer that is selected in table.
     * */
    private void deleteCustomer(Customer customer) throws Exception {
        System.out.println("***** Begin Delete Customer *****");
        try {
            PreparedStatement ps = ConnectDB.makeConnection().prepareStatement("DELETE FROM appointments where Customer_ID = ?");
            System.out.println("Delete appointments for CustomerID: " + customer.getCustomerID());
            ps.setInt(1, customer.getCustomerID());
            int result = ps.executeUpdate();

            PreparedStatement ps1 = ConnectDB.makeConnection().prepareStatement("DELETE FROM customers where Customer_ID = ?");
            System.out.println("Delete customer with CustomerID: " + customer.getCustomerID());
            ps1.setInt(1, customer.getCustomerID());
            int result1 = ps1.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Delete Customer SQL statement contains an error!");
        }
        clearTextFields();
        disableCustomerFields();
        updateCustomerTable();
        System.out.println("***** End Delete Customer *****");
    }

    /** This method updates current customer with all changes. */
    private void updateCustomer() throws Exception {
        System.out.println("***** Begin Update Customer *****");
        try {
            PreparedStatement ps = ConnectDB.makeConnection().prepareStatement("UPDATE customers SET Customer_Name  = ?, Address = ?, postal_Code = ?, phone = ?, division_ID = ?, customers.last_Update = CURRENT_TIMESTAMP, customers.last_Updated_By = ? WHERE customers.Customer_ID = ? ");

            ps.setString(1, custNameEntryBox.getText());
            ps.setString(2, custAddressEntryBox.getText());
            ps.setString(3, zipCodeEntryBox.getText());
            ps.setString(4, phoneEntryBox.getText());
            ps.setInt(5, getDivisionID(comboBoxFirstLevel.getValue()));
            ps.setString(6, User.getUsername());
            ps.setString(7, custIDEntryBox.getText());
            System.out.println("CustomerID for Update: " + custIDEntryBox.getText());

            int result = ps.executeUpdate();

        } catch (SQLException ex) {
            System.out.println("Update Customer SQL statement has an error!");
        }
        clearTextFields();
        disableCustomerFields();
        updateCustomerTable();
        customerAdd = false;
        customerUpdate = false;
        System.out.println("***** End Update Customer *****");
    }

    /** This method checks to see if Customer data fields are valid before save/update.
     * @return Returns False or True based on validity checks.
     * */
    private boolean validCustomer() {
        String customerName = custNameEntryBox.getText().trim();
        String address = custAddressEntryBox.getText().trim();
        String division = comboBoxFirstLevel.getValue().trim();
        String country = comboBoxCounty.getValue().trim();
        String postalCode = zipCodeEntryBox.getText().trim();
        String phone = phoneEntryBox.getText().trim();

        String errorMessage = "";
        //first checks to see if inputs are null
        if (customerName == null || customerName.length() == 0) {
            errorMessage += rb.getString("nameerror") + "\n";
        }
        if (address == null || address.length() == 0) {
            errorMessage += rb.getString("addresserror") + "\n";
        }
        if (division == null) {
            errorMessage += rb.getString("divisionerror") + "\n";
        }
        if (country == null) {
            errorMessage += rb.getString("countryerror") + "\n";
        }
        if (postalCode == null || postalCode.length() == 0) {
            errorMessage += rb.getString("postalerror") + "\n";
        } else if (postalCode.length() > 10 || postalCode.length() < 5) {
            errorMessage += rb.getString("postallengtherror") + "\n";
        }
        if (phone == null || phone.length() == 0) {
            errorMessage += rb.getString("phoneerror") + "\n";
        } else if (phone.length() < 10 || phone.length() > 15) {
            errorMessage += rb.getString("validphoneerror") + "\n";
        }
        if (errorMessage.length() == 0) {
            return true;
        } else {
            // Show the error message.
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(rb.getString("error"));
            alert.setHeaderText(rb.getString("invalidcustomeralert"));
            alert.setContentText(errorMessage);
            Optional<ButtonType> result = alert.showAndWait();

            return false;
        }
    }

    /** This method gets division ID based on division name.
     * @param division The division name selected.
     * @return Returns division ID.
     * */
    private int getDivisionID(String division) throws SQLException, Exception {
        int divisionID = -1;

        //create statement object
        Statement statement = ConnectDB.makeConnection().createStatement();

        //write SQL statement
        String sqlStatement = "SELECT division_ID FROM first_level_divisions WHERE first_level_divisions.division ='" + division + "'";

        //create resultset object
        ResultSet result = statement.executeQuery(sqlStatement);

        while (result.next()) {
            divisionID = result.getInt("division_Id");
        }
        return divisionID;
    }

    /** This method prevents inputs in customer fields when not adding or updating a customer. */
    public void disableCustomerFields(){
        custIDEntryBox.setDisable(true);
        custNameEntryBox.setDisable(true);
        custAddressEntryBox.setDisable(true);
        comboBoxFirstLevel.setDisable(true);
        comboBoxCounty.setDisable(true);
        zipCodeEntryBox.setDisable(true);
        phoneEntryBox.setDisable(true);
        buttonToSave.setDisable(true);
        buttonToCancel.setDisable(true);
        CustomerDeleteButton.setDisable(true);
    }

    /** This method allows inputs to add/update customer. */
    public void enableCustomerFields(){
        custIDEntryBox.setDisable(false);
        custIDEntryBox.setEditable(false);
        custNameEntryBox.setDisable(false);
        custAddressEntryBox.setDisable(false);
        comboBoxFirstLevel.setDisable(false);
        comboBoxCounty.setDisable(false);
        zipCodeEntryBox.setDisable(false);
        phoneEntryBox.setDisable(false);
        buttonToSave.setDisable(false);
        buttonToCancel.setDisable(false);
        CustomerDeleteButton.setDisable(false);
    }

    /** This method is called when customer table listener detects customer selection to update customer.
     * @param customer Customer selected in customer table.
     * */
    public void customerListener(Customer customer) throws SQLException {
        System.out.println("***** Begin Customer Listener *****");
        Customer cust = CustomerTable.getSelectionModel().getSelectedItem();
        if (cust != null) {
            int custId = cust.getCustomerID();
            addUpdateLabel.setText("Modify Existing Customer");
            customerUpdate = true;
            customerAdd = false;
            enableCustomerFields();

            //create statement object
            PreparedStatement ps = ConnectDB.makeConnection().prepareStatement("SELECT customers.customer_Id, customer_Name, phone, address, Postal_Code, first_level_divisions.Division, first_level_divisions.Division_ID, countries.Country FROM customers, first_level_divisions, countries WHERE customers.customer_ID = ? AND customers.Division_ID = first_level_divisions.Division_ID AND first_level_divisions.Country_ID = countries.Country_ID");

            //execute statement and create resultset object
            ps.setInt(1, custId); //-----------not sure------
            ResultSet result = ps.executeQuery();
            System.out.println("SQL Statement: " + ps);
            while (result.next()) {
                System.out.println("CustomerID: " + result.getInt("customer_Id"));
                custIDEntryBox.setText(Integer.toString(result.getInt("customer_Id")));
                custNameEntryBox.setText(result.getString("customer_Name"));
                custAddressEntryBox.setText(result.getString("address"));
                comboBoxCounty.setValue(result.getString("country"));
                comboBoxFirstLevel.setValue(result.getString("division"));
                zipCodeEntryBox.setText(result.getString("Postal_Code"));
                phoneEntryBox.setText(result.getString("phone"));
                System.out.println("***** End Customer Listener *****");
            }
        }
    }

    /** This method handles the appointment cancel button.
     * When the button is clicked nothing will be saved and text fields will be cleared.
     * @param event In this case a mouse clicking the cancel button.
     * */
    @FXML
    private void buttonToCancelHandler(ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(rb.getString("confirmationrequired"));
        alert.setHeaderText(rb.getString("confirmcancel"));
        alert.setContentText(rb.getString("areyousure"));
        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK) {
            customerUpdate = false;
            customerAdd = false;
            clearTextFields();
            disableCustomerFields();
        } else {
            System.out.println("Cancel canceled.");
        }
    }

    /** This method handles the add button.
     * When the button is clicked it enables the text fields and comboboxes for customer data entry.
     * @param event A mouse click of the Add button.
     * */
    @FXML
    private void CustomerAddButtonHandler(ActionEvent event) throws SQLException {
        clearTextFields();
        addUpdateLabel.setText("Create New Customer");
        custIDEntryBox.setText("Auto Generated");
        customerAdd = true;
        customerUpdate = false;
        enableCustomerFields();
    }

    /** This method handles the delete button.
     * When the button is clicked it deletes the selected customer from the customer table.
     * @param event A mouse click of the delete button.
     * */
    @FXML
    private void CustomerDeleteButtonHandler(ActionEvent event) throws Exception {
        if (CustomerTable.getSelectionModel().getSelectedItem() != null) {

            Customer cust = CustomerTable.getSelectionModel().getSelectedItem();
            String custName = cust.getCustomerName();
            String custPhone = cust.getCustomerPhone();
            System.out.println("Name: " + custName);
            System.out.println("Phone: " + custPhone);
            System.out.println("CustomerID : " + cust.getCustomerID());

            //String customerName = customer.getCustomerName();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(rb.getString("confirmationrequired"));
            alert.setHeaderText(rb.getString("confirmationdelete"));
            alert.setContentText(rb.getString("confirmdeletealertcontent") + cust.getCustomerID() + "?");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.get() == ButtonType.OK) {
                System.out.println("Deleting customer...");
                deleteCustomer(cust);
                System.out.println("CustomerID: " + cust.getCustomerID() + " has been deleted!");

                clearTextFields();
                disableCustomerFields();
                updateCustomerTable();
            } else {
                System.out.println("DELETE was canceled.");
            }
        } else {
            System.out.println("No customer was selected to delete!");
        }
    }

    /** This method handles the back button.
     * When the button is clicked it takes you back to the main screen.
     * @param event A mouse click of the back button.
     * */
    @FXML
    private void buttonToGoBackHandler(ActionEvent event) throws IOException {
        parent = FXMLLoader.load(getClass().getResource("/view/MainScreen.fxml"));
        setup = (Stage) buttonToGoBack.getScene().getWindow();
        Scene scene = new Scene(parent);
        setup.setScene(scene);
        setup.show();
    }
}

