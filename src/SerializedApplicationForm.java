import java.io.Serializable;
import java.util.Scanner;

public class SerializedApplicationForm implements Serializable {
    private static final long serialVersionUID = 73L;
    private static Scanner scanner = new Scanner(System.in);

    private String firstName, lastName;
    private String uniqueId, email , phNo, password;
    private String entrance, regNo;
    private double obtainedMarks, percentile;
    private String hscBoard, hscRegNo;
    private double hscPercentage;
    private String branchName;

    public SerializedApplicationForm() {
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public String getEmail() {
        return email;
    }

    public String getPhNo() {
        return phNo;
    }

    public String getRegNo() {
        return regNo;
    }

    public double getObtainedMarks() {
        return obtainedMarks;
    }

    public String getPassword() {
        return password;
    }

    public double getPercentile() {
        return percentile;
    }

    public String getHscBoard() {
        return hscBoard;
    }

    public String getHscRegNo() {
        return hscRegNo;
    }

    public double getHscPercentage() {
        return hscPercentage;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhNo(String phNo) {
        this.phNo = phNo;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setExam(String entrance) {
        this.entrance = entrance;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public void setObtainedMarks(double obtainedMarks) {
        this.obtainedMarks = obtainedMarks;
    }

    public void setPercentile(double percentile) {
        this.percentile = percentile;
    }

    public void setHscBoard(String hscBoard) {
        this.hscBoard = hscBoard;
    }

    public void setHscRegNo(String hscRegNo) {
        this.hscRegNo = hscRegNo;
    }

    public void setHscPercentage(double hscPercentage) {
        this.hscPercentage = hscPercentage;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    @Override
    public String toString() {
        String publicPassword = new String("*").repeat(password.length());
        return "{" +
                "\n\tfirstName: '" + firstName + '\'' +
                ",\n\tlastName: '" + lastName + '\'' +
                ",\n\tuniqueId: '" + uniqueId + '\'' +
                ",\n\temail: '" + email + '\'' +
                ",\n\tphNo: '" + phNo + '\'' +
                ",\n\tpassword: '" + publicPassword + '\'' +
                ",\n\tentrance: '" + entrance + '\'' +
                ",\n\tregNo: '" + regNo + '\'' +
                ",\n\tobtainedMarks: " + obtainedMarks +
                ",\n\tpercentile: " + percentile +
                ",\n\thscBoard: '" + hscBoard + '\'' +
                ",\n\thscRegNo: '" + hscRegNo + '\'' +
                ",\n\thscPercentage: " + hscPercentage +
                ",\n\tbranchName: '" + branchName + '\'' +
                "\n}";
    }
}
