package controller;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import util.ConnectDB;

/** FXML Controller class.
 * This controls the main screen.
 * @author Nicholas Fallico
 */
public class MainScreenController implements Initializable {
    @FXML
    public Button customerScreenButton;

    //Declare variables
    Parent parent;
    Stage setup;

    /** Initializes the controller class.
     * @param url The URL parameter.
     * @param rb The ResourceBundle parameter.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Nothing needs to be initialized
    }

    /** This method handles the customers button.
     * @param event When clicked the customer screen is loaded.
     */
    @FXML
    private void customerScreenButtonHandler(ActionEvent event) throws IOException {
        parent = FXMLLoader.load(getClass().getResource("/view/CustomerScreen.fxml"));
        setup = (Stage)customerScreenButton.getScene().getWindow();
        Scene scene = new Scene(parent);
        setup.setScene(scene);
        setup.show();
    }

    /** This method handles the appointments buttons.
     * @param event When clicked the appointment screen is loaded.
     * */
    @FXML
    private void appointmentScreenButton(ActionEvent event) throws IOException {
        parent = FXMLLoader.load(getClass().getResource("/view/MainAppointments.fxml"));
        setup = (Stage)customerScreenButton.getScene().getWindow();
        Scene scene = new Scene(parent);
        setup.setScene(scene);
        setup.show();
    }

    /** This method handles the reports button.
     * @param event When clicked it loads the reports page.
     * */
    @FXML
    private void reportScreenButton(ActionEvent event) throws IOException {
        parent = FXMLLoader.load(getClass().getResource("/view/Reports.fxml"));
        setup = (Stage)customerScreenButton.getScene().getWindow();
        Scene scene = new Scene(parent);
        setup.setScene(scene);
        setup.show();
    }

    /** This method handles the exit button.
     * @param event When clicked it closes the program.
     * */
    @FXML
    private void endProgramButton(ActionEvent event) throws SQLException {
        ResourceBundle rb = ResourceBundle.getBundle("properties.login", Locale.getDefault());

        Alert signal = new Alert(Alert.AlertType.CONFIRMATION);
        signal.setTitle(rb.getString("exitalerttitle"));
        signal.setHeaderText(rb.getString("exitalertheader"));
        signal.setContentText(rb.getString("exitalertcontent"));
        Optional<ButtonType> set = signal.showAndWait();

        if (set.get() == ButtonType.OK) {
            ConnectDB.closeConnection();
            System.out.println("Program Exit.");
            System.exit(0);
        }
        else{
            System.out.println("Exit canceled.");
        }
    }
}

