package model;


import java.sql.Date;


/** This class handles the Customer constructors, setters, and getters.
 * @author Nicholas Fallico.
 * */
public class Customer {

    private static String country;
    private int customerID; //Auto incremented in database
    private String customerName;
    private Date createDate;
    private String createdBy;
    private String address;
    private String division;
    private String postalCode;
    private String phone;
    private Date lastUpdate;
    private String lastUpdateBy;

    //constructor
    public Customer() {
    }

    public Customer(int customerID, String customerName, String address, String division, String postalCode, String phone, String country, Date lastUpdate, String lastUpdateBy) {
        setCustomerID(customerID);
        setCustomerName(customerName);
        setCustomerAddress(address);
        setDivision(division);
        setCustomerPostalCode(postalCode);
        setCustomerPhone(phone);
        setCustomerCountry(country);
        setCustomerLastUpdate(lastUpdate);
        setCustomerLastUpdateBy(lastUpdateBy);
    }

    public Customer(int customerID, String customerName) {
        setCustomerID(customerID); //this is Auto Incremented in the database
        setCustomerName(customerName);
    }

    //getters

    public int getCustomerID() {
        return customerID;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerAddress() {
        return address;
    }

    public String getDivision() {
        return division;
    }

    public String getCustomerPostalCode() {
        return postalCode;
    }

    public String getCustomerPhone() {
        return phone;
    }

    public static String getCustomerCountry() {
        return country;
    }

    public Date getCustomerLastUpdate() {
        return lastUpdate;
    }

    public String getCustomerLastUpdateBy() {
        return lastUpdateBy;
    }


    //setters

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setCustomerAddress(String address) {
        this.address = address;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public void setCustomerPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public void setCustomerPhone(String phone) {
        this.phone = phone;
    }

    public void setCustomerCountry(String country) {
        this.country = country;
    }

    public void setCustomerLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void setCustomerLastUpdateBy(String lastUpdateBy) {
        this.lastUpdateBy = lastUpdateBy;
    }

}

