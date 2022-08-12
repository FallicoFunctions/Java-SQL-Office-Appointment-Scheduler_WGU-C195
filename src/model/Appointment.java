package model;


import java.sql.Date;
import java.sql.Timestamp;

/** This class handles the Appointment constructors, setters, and getters.
 * @author Nicholas Fallico.
 * */
public class Appointment {

    private int appointmentID;//auto generated
    private int customerID;//auto generated
    private int userID;//auto generated
    private String contactID;
    private Customer customer;
    private String title;
    private String description;
    private String location;
    private String contact;
    private String type;
    private String url;
    private String startTime;
    private String endTime;
    private String customerName;
    private Date createDate;
    private String createdBy;
    private Timestamp lastUpdate;
    private String lastUpdatedBy;
    private String localStartDT;
    private String localEndDT;


    public Appointment(int appointmentID, int customerID, int userID, String title, String description, String location,
                       String contact, String type, String contactID, String startTime, String endTime, Date createDate, String createdBy, Timestamp lastUpdate, String lastUpdatedBy){

        setAppointmentID(appointmentID);//auto generated
        setCustomerID(customerID);//auto generated
        setUserID(userID);//auto generated
        setTitle(title);
        setDescription(description);
        setLocation(location);
        setContact(contact);
        setType(type);
        setContactID(contactID);
        setStart(startTime);
        setEnd(endTime);
        setCreateDate(createDate);
        setCreatedBy(createdBy);
        setLastUpdate(lastUpdate);
        setLastUpdatedBy(lastUpdatedBy);
    }


    public Appointment(int appointmentID, String startTime, String endTime, String title, String type, int customerId, String customerName, String user){
        setAppointmentID(appointmentID);
        setStart(startTime);
        setEnd(endTime);
        setTitle(title);
        setType(type);
        setCustomerID(customerId);
        setCustomerName(customerName);
        setCreatedBy(user);
    }

    public Appointment(int appointmentID, int customerID, int userID, String title, String description, String location, String contact, String type,
                       /*String url,*/ String startTime, String endTime, String customerName, String user){

        setAppointmentID(appointmentID);//auto generated
        setCustomerID(customerID);//auto generated
        setUserID(userID);//auto generated
        setTitle(title);
        setDescription(description);
        setLocation(location);
        setContact(contact);
        setType(type);
        setStart(startTime);
        setEnd(endTime);
        setCustomerName(customerName);
        setCreatedBy(user);

    }
    public Appointment(String startTime, String endTime, String title, String type, String customer, String user) {
        setStart(startTime);
        setEnd(endTime);
        setTitle(title);
        setType(type);
        setCustomerName(customer);
        setCreatedBy(user); //contact equals User - not sure
    }

    public Appointment(){
    }

    public Appointment(int appointmentID, int customerID, String title, String description, String type, String start, String end) {
        setAppointmentID(appointmentID);
        setCustomerID(customerID);
        setTitle(title);
        setDescription(description);
        setType(type);
        setStart(start);
        setEnd(end);
    }

    //getters
    public int getAppointmentID(){
        return this.appointmentID;
    }

    public int getCustomerID(){
        return this.customerID;
    }

    public int getUserID(){
        return this.userID;
    }

    public String getTitle(){
        return this.title;
    }

    public String getDescription(){
        return this.description;
    }

    public String getLocation(){
        return this.location;
    }

    public String getContact(){
        return this.contact;
    }

    public String getType(){
        return this.type;
    }

    public String getStart(){
        return this.startTime;
    }

    public String getEnd(){
        return this.endTime;
    }

    public Date getCreateDate(){
        return this.createDate;
    }

    public String getCreatedBy(){
        return this.createdBy;
    }

    public Timestamp getLastUpdate(){
        return this.lastUpdate;
    }

    public String getLastUpdatedBy(){
        return this.lastUpdatedBy;
    }

    public String getCustomerName(){
        return this.customerName;
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getContactID() {
        return this.contactID;
    }

    //setters

    private void setlocalEndDT(String localEndDT) {
        this.localEndDT = localEndDT;
    }

    private void setLocalStartDT(String localStartDT) {
        this.localStartDT = localStartDT;
    }

    public void setContactID(String contactID) {
        this.contactID = contactID;
    }

    private void setCustomer(Customer customer){
        this.customer = customer;
    }

    private void setAppointmentID(int appointmentID){
        this.appointmentID = appointmentID;//auto generated
    }

    private void setCustomerID(int customerID){
        this.customerID = customerID;//auto generated
    }

    private void setUserID(int userID){
        this.userID = userID;//auto generated
    }

    private void setTitle(String title){
        this.title = title;
    }

    private void setDescription(String description){
        this.description = description;
    }

    private void setLocation(String location){
        this.location = location;
    }

    public void setContact(String contact){
        this.contact = contact;
    }

    private void setType(String type){
        this.type = type;
    }

    private void setStart(String startTime){
        this.startTime = startTime;
    }

    private void setEnd(String endTime){
        this.endTime = endTime;
    }

    private void setCreateDate(Date createDate){
        this.createDate = createDate;
    }

    private void setCreatedBy(String createdBy){
        this.createdBy = createdBy;
    }

    private void setLastUpdate(Timestamp lastUpdate){
        this.lastUpdate = lastUpdate;
    }

    private void setLastUpdatedBy(String lastUpdatedBy){
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public void setCustomerName(String customerName){
        this.customerName = customerName;
    }

    public void setcontact(String contact){
        this.contact = contact;
    }
}