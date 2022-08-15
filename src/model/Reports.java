package model;


/** This class handles the Report constructors, setters, and getters.
 * @author Nicholas Fallico.
 * */
public class Reports {

    private String month;
    private int firstTimeVisit;
    private int generalCheckup;
    private int bloodWorkVisit;
    private int psychiatricVisit;
    private int email;
    private int phone;
    private int inperson;
    private int typesArray[];

    //constructors
    public Reports(String month, int typesArray[]) {
        setMonth(month);
        setTypesArray(typesArray);
    }

    public Reports(String month, int firstTimeVisit, int generalCheckup, int bloodWorkVisit, int psychiatricVisit){
        setMonth(month);
        setFirstTimeVisit(firstTimeVisit);
        setGeneralCheckup(generalCheckup);
        setBloodWorkVisit(bloodWorkVisit);
        setPsychiatricVisit(psychiatricVisit);
    }

    public Reports(String month, int email, int phone, int inperson){
        setMonth(month);
        setEmail(email);
        setPhone(phone);
        setInperson(inperson);
    }

    //getters
    public String getMonth() {
        return this.month;
    }

    public int[] getTypesArray() {
        return this.typesArray;
    }

    public int getFirstTimeVisit(){
        return this.firstTimeVisit;
    }

    public int getGeneralCheckup(){
        return this.generalCheckup;
    }

    public int getBloodWorkVisit(){
        return this.bloodWorkVisit;
    }

    public int getPsychiatricVisit(){
        return this.psychiatricVisit;
    }

    public int getEmail(){
        return this.email;
    }

    public int getPhone(){
        return this.phone;
    }

    public int getInperson(){
        return this.inperson;
    }

    //setters
    private void setMonth(String month) {
        this.month = month;
    }

    private void setFirstTimeVisit(int firstTimeVisit){
        this.firstTimeVisit = firstTimeVisit;
    }

    private void setGeneralCheckup(int generalCheckup){
        this.generalCheckup = generalCheckup;
    }

    private void setBloodWorkVisit(int bloodWorkVisit){
        this.bloodWorkVisit = bloodWorkVisit;
    }

    private void setPsychiatricVisit(int psychiatricVisit){
        this.psychiatricVisit = psychiatricVisit;
    }

    private void setEmail(int email){
        this.email = email;
    }

    private void setPhone(int phone){
        this.phone = phone;
    }

    private void setInperson(int inperson){
        this.inperson = inperson;
    }

    private void setTypesArray(int[] typesArray) {
        this.typesArray = typesArray;
    }
}

