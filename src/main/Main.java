package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import util.ConnectDB;

import java.sql.SQLException;
import java.util.Locale;

/** This class creates an app for an appointment scheduler. */
public class Main extends Application {

    /** This method loads the login screen.
     * @param stage The first stage called.
     * */
    @Override
    public void start(Stage stage) throws Exception {
        //Locale.setDefault(new Locale("fr")); //----Use this hardcode program to french for testing
        System.out.println("Start...");
        Parent parent = FXMLLoader.load(getClass().getResource("/view/Login.fxml"));
        Scene scene = new Scene(parent);
        stage.setScene(scene);
        stage.show();
        System.out.println("End of Start section");
    }

    /**
     * @param args the command line arguments
     * @throws java.lang.ClassNotFoundException
     * @throws java.sql.SQLException
     */
    public static void main(String[] args) throws SQLException, Exception {
        System.out.println(Locale.getDefault().toString());
        ConnectDB.makeConnection();
        launch(args);
        ConnectDB.closeConnection();
    }
}
