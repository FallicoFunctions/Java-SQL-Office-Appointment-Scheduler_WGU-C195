package controller;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    public Label addUpdateLabel;
    @FXML
    public TableView<Customer> displayCustomerList;
    @FXML
    public TableColumn<Customer, Integer> columnID;
    @FXML
    public TableColumn<Customer, String> columnName;
    @FXML
    public TableColumn<Customer, String> columnPhone;
    @FXML
    public TextField custIDEntryBox;
    @FXML
    public ComboBox<String> comboBoxFirstLevel;
    @FXML
    public ComboBox<String> comboBoxCounty;
    @FXML
    public Button buttonToSave;
    @FXML
    public Button buttonToCancel;
    @FXML
    public Button CustomerDeleteButton;
    @FXML
    public TextField custNameEntryBox;
    @FXML
    public TextField custAddressEntryBox;
    @FXML
    public TextField zipCodeEntryBox;
    @FXML
    public TextField phoneEntryBox;
    @FXML
    public Button buttonToGoBack;

    //Declare Variables and lists
    ObservableList<Customer> listOfCusts = FXCollections.observableArrayList();
    ObservableList<String> firstLevelOptions = FXCollections.observableArrayList();
    ObservableList<String> listOfCountries = FXCollections.observableArrayList();
    Parent parent;
    Stage setup;
    boolean reviseCust = false; //Boolean for revising current customers
    boolean newCust = false; //Boolean for saving new customers
    ResourceBundle rb = ResourceBundle.getBundle("properties.login", Locale.getDefault());

    /** Initializes the controller class.
     * @param url The URL parameter.
     * @param rb The ResourceBundle parameter.
     * <p>
     * There is a lambda listener expression on line 122. It is justified because it listens for the mouse click in the
     * customer table. Then it populates the customer form with the selected customer's info. It is more efficient
     * to use a lambda for this task.
     * </p>
     * */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Input customer data into the tableview
        columnID.setCellValueFactory(new PropertyValueFactory<>("CustomerID"));
        columnName.setCellValueFactory(new PropertyValueFactory<>("CustomerName"));
        columnPhone.setCellValueFactory(new PropertyValueFactory<>("CustomerPhone"));
        columnAddress.setCellValueFactory(new PropertyValueFactory<>("CustomerAddress"));
        columnDivision.setCellValueFactory(new PropertyValueFactory<>("Division"));
        columnPostalCode.setCellValueFactory(new PropertyValueFactory<>("CustomerPostalCode"));

        custIDEntryBox.setText("Auto Generated");
        turnOffEntryBoxes();
        try {
            System.out.println("Current userID: " + User.getUserID());
            System.out.println("Current Username: " + User.getUsername());
            loadCustomerData();
            try {
                fillCountryComboBox();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        displayCustomerList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    try {
                        customerListener(newValue);
                    } catch (SQLException ex) {
                        System.out.println("Customer Listener had an error!");
                        ex.printStackTrace();;
                    }
                });
    }

    /** This method inputs customers data into the tableview. */
    public void loadCustomerData() throws SQLException {
        System.out.println("***** Begin Update Customer Table *****");
        listOfCusts.clear();
        String sqlStatement = "SELECT customer_Id, customer_Name, phone, address, Postal_Code, first_level_divisions.Division, first_level_divisions.Division_ID, Country_ID FROM customers, first_level_divisions WHERE customers.Division_ID = first_level_divisions.Division_ID ORDER BY customers.customer_Name";
        PreparedStatement sql = ConnectDB.makeConnection().prepareStatement(sqlStatement);
        ResultSet set = sql.executeQuery(sqlStatement);
        while (set.next()) {
            Customer current = new Customer();
            current.setCustomerID(set.getInt("customer_Id"));
            current.setCustomerName(set.getString("customer_Name"));
            current.setCustomerAddress(set.getString("address"));
            current.setCustomerPhone(set.getString("phone"));
            current.setCustomerPostalCode(set.getString("postal_code"));
            current.setDivision(set.getString("division"));
            listOfCusts.addAll(current);
        }
        displayCustomerList.setItems(listOfCusts);
        System.out.println("***** End Update Customer Table *****");
    }

    /** This method clears all comboboxes and text fields. */
    public void emptyEntryBoxes() {
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
        System.out.println(countryName);
        firstLevelOptions.clear();
        if (countryName != null) {
            try {
                PreparedStatement sql = ConnectDB.makeConnection().prepareStatement("SELECT first_level_divisions.* FROM first_level_divisions, countries WHERE first_level_divisions.Country_ID = countries.country_ID AND countries.country = ?");
                sql.setString(1, countryName);
                ResultSet set = sql.executeQuery();
                while (set.next()) {
                    firstLevelOptions.add(set.getString("division"));
                }
                comboBoxFirstLevel.setItems(firstLevelOptions);
                sql.close();
                set.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    /** This method populates CountryComboBox with all available countries. */
    public void fillCountryComboBox() throws Exception {
        PreparedStatement sql = ConnectDB.makeConnection().prepareStatement("SELECT country FROM countries");
        ResultSet set = sql.executeQuery();
        while (set.next()) {
            Customer cust = new Customer();
            cust.setCustomerCountry(set.getString("country"));
            listOfCountries.add(cust.getCustomerCountry());
            comboBoxCounty.setItems(listOfCountries);
        }
        sql.close();
        set.close();
    }

    /** This method calls the firstLevelBox method when this handler is activated.
     * @param event When actioned it calls the fillFirstLevelBox method.
     * <p>
     * There is a lambda listener expression on line 211. It is justified because it listens for the mouse click in the
     * country combobox. Then it populates the first level box with the selected country's division. It is more efficient
     * to use a lambda for this task.
     * </p>
     * */
    @FXML
    public void comboBoxCountyHandler(ActionEvent event) {
        comboBoxCounty.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    try {
                        fillFirstLevelBox(newValue);
                    } catch (Exception ex) {
                        System.out.println("Customer Listener had an error!");
                        ex.printStackTrace();
                    }
                });
    }

    /** This method calls other methods to save customer info.
     * insertNewCustSQL method if new customer.
     * insertExistingCustSQL method if existing customer.
     * @param event When actioned it calls either of the mentioned methods.
     * */
    @FXML
    public void buttonToSaveHandler(ActionEvent event) throws Exception {
        System.out.println("newCust: " + newCust);
        System.out.println("reviseCust: " + reviseCust);
        if (custNameEntryBox.getText() != null && newCust || reviseCust) {
            if (validCustomer()) {
                if (newCust) {
                    insertNewCustSQL();
                    emptyEntryBoxes();
                    loadCustomerData();
                } else if (reviseCust) {
                    insertExistingCustSQL();
                    emptyEntryBoxes();
                    loadCustomerData();
                }
            }
        } else {
            System.out.println("No customer selected to save!");
        }
    }

    /** This method inserts newly created customer data into the SQL database. */
    public void insertNewCustSQL() throws Exception {
        System.out.println("***** Begin Save Customer *****");
        try {
            PreparedStatement sql = ConnectDB.makeConnection().prepareStatement("INSERT INTO customers (customer_Name, address, postal_code, phone, create_Date, created_By, last_Update, last_Updated_By, division_Id) "
                    + "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?, ?)");

            sql.setString(1, custNameEntryBox.getText());
            sql.setString(2, custAddressEntryBox.getText());
            sql.setString(3, zipCodeEntryBox.getText());
            sql.setString(4, phoneEntryBox.getText());
            sql.setString(5, User.getUsername());
            sql.setString(6, User.getUsername());
            sql.setInt(7, getDivisionID(comboBoxFirstLevel.getValue()));
            int sets = sql.executeUpdate();
            sql.close();
        } catch (SQLException sql) {
            System.out.println("SQL statement has an error!");
            sql.printStackTrace();
        }
        emptyEntryBoxes();
        turnOffEntryBoxes();
        loadCustomerData();
        newCust = false;
        reviseCust = false;
        System.out.println("***** End Save Customer *****");
    }

    /** This method deletes the selected customer in customer table.
     * @param customer Customer that is selected in table.
     * */
    public void removeCustomer(Customer customer) throws Exception {
        System.out.println("***** Begin Delete Customer *****");
        try {
            PreparedStatement sql = ConnectDB.makeConnection().prepareStatement("DELETE FROM appointments where Customer_ID = ?");
            System.out.println("Delete appointments for CustomerID: " + customer.getCustomerID());
            sql.setInt(1, customer.getCustomerID());
            int sets = sql.executeUpdate();

            PreparedStatement sql2 = ConnectDB.makeConnection().prepareStatement("DELETE FROM customers where Customer_ID = ?");
            System.out.println("Delete customer with CustomerID: " + customer.getCustomerID());
            sql2.setInt(1, customer.getCustomerID());
            int sets1 = sql2.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Delete Customer SQL statement contains an error!");
        }
        emptyEntryBoxes();
        turnOffEntryBoxes();
        loadCustomerData();
        System.out.println("***** End Delete Customer *****");
    }

    /** This method inputs SQL updates for customer data. */
    public void insertExistingCustSQL() throws Exception {
        System.out.println("***** Begin Update Customer *****");
        try {
            PreparedStatement sql = ConnectDB.makeConnection().prepareStatement("UPDATE customers SET Customer_Name  " +
                    "= ?, Address = ?, postal_Code = ?, phone = ?, division_ID = ?, customers.last_Update = " +
                    "CURRENT_TIMESTAMP, customers.last_Updated_By = ? WHERE customers.Customer_ID = ? ");

            sql.setString(1, custNameEntryBox.getText());
            sql.setString(2, custAddressEntryBox.getText());
            sql.setString(3, zipCodeEntryBox.getText());
            sql.setString(4, phoneEntryBox.getText());
            sql.setInt(5, getDivisionID(comboBoxFirstLevel.getValue()));
            sql.setString(6, User.getUsername());
            sql.setString(7, custIDEntryBox.getText());
            System.out.println("CustomerID for Update: " + custIDEntryBox.getText());
            int sets = sql.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("Update Customer SQL statement has an error!");
        }
        emptyEntryBoxes();
        turnOffEntryBoxes();
        loadCustomerData();
        newCust = false;
        reviseCust = false;
        System.out.println("***** End Update Customer *****");
    }

    /** This method checks to see if Customer data fields are valid before save/update.
     * @return Returns False or True based on validity checks.
     * */
    public boolean validCustomer() {
        String nameOfCust = custNameEntryBox.getText();
        String custAddress = custAddressEntryBox.getText();
        String division = comboBoxFirstLevel.getValue();
        String custCountry = comboBoxCounty.getValue();
        String custZipCode = zipCodeEntryBox.getText();
        String custTelephone = phoneEntryBox.getText();

        //The below if-statements verify if the customer entry fields are blank
        String isValid = "";
        if (nameOfCust.length() == 0) {
            isValid += rb.getString("nameerror") + System.lineSeparator();
        }
        if (custAddress.length() == 0) {
            isValid += rb.getString("addresserror") + System.lineSeparator();
        }
        if (division == null) {
            isValid += rb.getString("divisionerror") + System.lineSeparator();
        }
        if (custCountry == null) {
            isValid += rb.getString("countryerror") + System.lineSeparator();
        }
        if (custZipCode.length() <5) {
            isValid += rb.getString("postalerror") + System.lineSeparator();
        }
        if (custTelephone.length() < 10) {
            isValid += rb.getString("phoneerror") + System.lineSeparator();
        }
        if (isValid.length() == 0) {
            return true;
        } else {
            Alert signal = new Alert(Alert.AlertType.ERROR);
            signal.setTitle(rb.getString("error"));
            signal.setHeaderText(rb.getString("invalidcustomeralert"));
            signal.setContentText(isValid);
            Optional<ButtonType> set = signal.showAndWait();
            return false;
        }
    }

    /** This method gets division ID based on division name.
     * @param division The division name selected.
     * @return Returns division ID.
     * */
    public int getDivisionID(String division) throws SQLException, Exception {
        int divisionID = -1;
        PreparedStatement sql = ConnectDB.makeConnection().prepareStatement( "SELECT division_ID FROM " +
                "first_level_divisions WHERE first_level_divisions.division ='" + division + "'");
        ResultSet set = sql.executeQuery();
        while (set.next()) {
            divisionID = set.getInt("division_Id");
        }
        return divisionID;
    }

    /** This method prevents inputs in customer fields when not adding or updating a customer. */
    public void turnOffEntryBoxes(){
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
    public void turnOnfEntryBoxes(){
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
        Customer current = displayCustomerList.getSelectionModel().getSelectedItem();
        if (current != null) {
            int custId = current.getCustomerID();

            addUpdateLabel.setText("Modify Existing Customer");
            reviseCust = true;
            newCust = false;

            turnOnfEntryBoxes();

            PreparedStatement sql = ConnectDB.makeConnection().prepareStatement("SELECT customers.customer_Id, customer_Name, phone, address, Postal_Code, first_level_divisions.Division, first_level_divisions.Division_ID, countries.Country FROM customers, first_level_divisions, countries WHERE customers.customer_ID = ? AND customers.Division_ID = first_level_divisions.Division_ID AND first_level_divisions.Country_ID = countries.Country_ID");
            sql.setInt(1, custId); //-----------not sure------
            ResultSet set = sql.executeQuery();
            System.out.println("SQL Statement: " + sql);
            while (set.next()) {
                System.out.println("CustomerID: " + set.getInt("customer_Id"));
                custIDEntryBox.setText(Integer.toString(set.getInt("customer_Id")));
                custNameEntryBox.setText(set.getString("customer_Name"));
                custAddressEntryBox.setText(set.getString("address"));
                comboBoxCounty.setValue(set.getString("country"));
                fillFirstLevelBox(comboBoxCounty.getValue());
                comboBoxFirstLevel.setValue(set.getString("division"));
                zipCodeEntryBox.setText(set.getString("Postal_Code"));
                phoneEntryBox.setText(set.getString("phone"));
                System.out.println("***** End Customer Listener *****");
            }
        }
    }

    /** This method handles the appointment cancel button.
     * When the button is clicked nothing will be saved and text fields will be cleared.
     * @param event In this case a mouse clicking the cancel button.
     * */
    @FXML
    public void buttonToCancelHandler(ActionEvent event) throws IOException {
        Alert signal = new Alert(Alert.AlertType.CONFIRMATION);
        signal.setTitle(rb.getString("confirmationrequired"));
        signal.setHeaderText(rb.getString("confirmcancel"));
        signal.setContentText(rb.getString("areyousure"));
        Optional<ButtonType> set = signal.showAndWait();
        if (set.get() == ButtonType.OK) {
            reviseCust = false;
            newCust = false;
            emptyEntryBoxes();
            turnOffEntryBoxes();
        } else {
            System.out.println("Cancel canceled.");
        }
    }

    /** This method handles the add button.
     * When the button is clicked it enables the text fields and comboboxes for customer data entry.
     * @param event A mouse click of the Add button.
     * */
    @FXML
    public void buttonToAddHandler(ActionEvent event) {
        emptyEntryBoxes();

        addUpdateLabel.setText("Create New Customer");
        custIDEntryBox.setText("Auto Generated");
        newCust = true;
        reviseCust = false;

        turnOnfEntryBoxes();
    }

    /** This method handles the delete button.
     * When the button is clicked it deletes the selected customer from the customer table.
     * @param event A mouse click of the delete button.
     * */
    @FXML
    public void buttonToDeleteHandler(ActionEvent event) throws Exception {
        if (displayCustomerList.getSelectionModel().getSelectedItem() != null) {
            Customer current = displayCustomerList.getSelectionModel().getSelectedItem();
            System.out.println("CustomerID : " + current.getCustomerID());
            String custName = current.getCustomerName();
            System.out.println("Name: " + custName);
            String custPhone = current.getCustomerPhone();
            System.out.println("Phone: " + custPhone);

            //Alert signal
            Alert signal = new Alert(Alert.AlertType.CONFIRMATION);
            signal.setTitle(rb.getString("confirmationrequired"));
            signal.setHeaderText(rb.getString("confirmationdelete"));
            signal.setContentText(rb.getString("confirmdeletealertcontent") + current.getCustomerID() + "?");
            Optional<ButtonType> set = signal.showAndWait();
            if (set.get() == ButtonType.OK) {
                System.out.println("Deleting customer...");
                removeCustomer(current);
                System.out.println("CustomerID: " + current.getCustomerID() + " has been deleted!");
                emptyEntryBoxes();
                turnOffEntryBoxes();
                loadCustomerData();
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
    public void buttonToGoBackHandler(ActionEvent event) throws IOException {
        parent = FXMLLoader.load(getClass().getResource("/view/MainScreen.fxml"));
        setup = (Stage) buttonToGoBack.getScene().getWindow();
        Scene scene = new Scene(parent);
        setup.setScene(scene);
        setup.show();
    }

    @FXML
    public void comboBoxFirstLevelHandler(ActionEvent actionEvent) {
        /*
        if(comboBoxCounty.getValue() != null) {
            comboBoxFirstLevel.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        try {
                            fillFirstLevelBox(comboBoxCounty.getValue());
                        } catch (Exception ex) {
                            System.out.println("Customer Listener had an error!");
                            ex.printStackTrace();
                        }
                    });
        } else {

            //Alert signal
            Alert signal = new Alert(Alert.AlertType.ERROR);
            signal.setTitle(rb.getString("nocountry"));
            signal.setHeaderText(rb.getString("countryerror"));
            signal.setContentText(rb.getString("clickokay"));
            Optional<ButtonType> set = signal.showAndWait();

        }*/
    }
}
