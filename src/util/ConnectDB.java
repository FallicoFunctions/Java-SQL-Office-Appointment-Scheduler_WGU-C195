package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** This class handles connections to the database.
 *  @author Nicholas Fallico
 * */
public class ConnectDB {
    //Declaring vars
    public static String databaseName = "client_schedule";
    public static String DB_URL = "jdbc:mysql://localhost:3306/" + databaseName;
    public static String username = "sqlUser";
    public static String password = "Passw0rd!";
    public static String driver = "com.mysql.cj.jdbc.Driver";
    public static Connection conn = null;

    /** This method establishes a connection to the database. */
    public static Connection makeConnection() {
        if (conn != null)
            return conn;
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(DB_URL, username, password);
            System.out.println("Connection successful.");
            return conn;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** This method closes the database connection. */
    public static void closeConnection() throws SQLException{
        conn.close();
        System.out.println("Connection closed.");
    }
}
