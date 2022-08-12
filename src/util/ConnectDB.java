package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** This class handles connections to the database.
 *  @author Nicholas Fallico
 * */
public class ConnectDB {

    //variables used to connect to database
    private static final String databaseName = "client_schedule";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/" + databaseName;
    private static final String username = "sqlUser";
    private static final String password = "Passw0rd!";
    private static final String driver = "com.mysql.cj.jdbc.Driver";
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
