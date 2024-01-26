import java.io.Serializable;

public class SerializedApplicant implements Serializable {
    private static final long serialVersionUID = 73L;

    private String firstName, lastName;
    private String uniqueId, email , contact;
    private String entranceRegNo;
    private double score, percentile;
    private String hscBoard, hscRegNo;
    private double hscPercentage;
    private String branchName;
    private String status, enrollmentId, applicantId;
    private boolean enrollmentForm;

    public SerializedApplicant(Applicant applicant) {
        this.firstName = applicant.getApplicationForm().getName().getFirst();
        this.lastName = applicant.getApplicationForm().getName().getLast();
        this.uniqueId = applicant.getApplicationForm().getUniqueIdNo();
        this.email = applicant.getApplicationForm().getEmail();
        this.contact = applicant.getApplicationForm().getPhNo();
        this.entranceRegNo = applicant.getApplicationForm().getExamination().getRegNo();
        this.score = applicant.getApplicationForm().getExamination().getObtainedMarks();
        this.percentile = applicant.getApplicationForm().getExamination().getPercentile();
        this.hscBoard = applicant.getApplicationForm().getHsc().getBoard();
        this.hscRegNo = applicant.getApplicationForm().getHsc().getRegNo();
        this.hscPercentage = applicant.getApplicationForm().getHsc().getPercentage();
        this.branchName = applicant.getApplicationForm().getBranchName();
        this.status = applicant.getStatus().name();
        this.applicantId = applicant.getApplicationId();
        this.enrollmentId = applicant.getEnrollmentId();
        this.enrollmentForm = (applicant.getEnrollmentForm()!=null);
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

    public String getContact() {
        return contact;
    }

    public String getEntranceRegNo() {
        return entranceRegNo;
    }

    public double getScore() {
        return score;
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

    public String getStatus() {
        return status;
    }

    public String getEnrollmentId() {
        return enrollmentId;
    }

    public String getApplicantId() {
        return applicantId;
    }

    public boolean hasEnrollmentForm() {
        return enrollmentForm;
    }

    @Override
    public String toString() {
        return "{" +
                "\n\tapplicantId: '" + applicantId + '\'' +
                "\n\tfirstName: '" + firstName + '\'' +
                ",\n\tlastName: '" + lastName + '\'' +
                ",\n\tuniqueId: '" + uniqueId + '\'' +
                ",\n\temail: '" + email + '\'' +
                ",\n\tcontact: '" + contact + '\'' +
                ",\n\tregNo: '" + entranceRegNo + '\'' +
                ",\n\tobtainedMarks: " + score +
                ",\n\tpercentile: " + percentile +
                ",\n\thscBoard: '" + hscBoard + '\'' +
                ",\n\thscRegNo: '" + hscRegNo + '\'' +
                ",\n\thscPercentage: " + hscPercentage +
                ",\n\tbranchName: '" + branchName + '\'' +
                ",\n\tstatus: '" + status + '\'' +
                ",\n\thasEnrollmentForm: " + enrollmentForm +
                ",\n\tenrollmentId: '" + enrollmentId + '\'' +
                "\n}";
    }
}
