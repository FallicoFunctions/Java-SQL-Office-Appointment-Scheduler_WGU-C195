package model;




/** This class handles the User constructors, setters, and getters.
 * @author Nicholas Fallico.
 * */
public class User {

    private static int userID; //auto incremented in database
    private static String username;
    private static String password;

    public User() {
        userID = 0;
        username = null;
        password = null;
    }

    //constructor
    public User(int userID, String username, String password) {
        this.userID = userID;
        this.username = username;
        this.password = password;
    }

    //getters
    public static int getUserID() {
        return userID;
    }

    public static String  getUsername() {
        return username;
    }

    public String getPassword() {
        return this.password;
    }

    //setters
    public static void setUserID(int userID) {
        User.userID = userID;
    }

    public static void setUsername(String username) {
        User.username = username;
    }

    public static void setPassword(String password) {
        User.password = password;
    }
}

