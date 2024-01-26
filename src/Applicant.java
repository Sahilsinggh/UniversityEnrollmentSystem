import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Applicant implements Comparable<Applicant>,Serializable {
    private static final long serialVersionUID = 73L;

    private String applicationId;
    private String password;
    private ApplicationForm applicationForm;

    private EnrollmentForm enrollmentForm;
    private String enrollmentId;

    private Status status;

    public Applicant(){
        applicationForm = new ApplicationForm();
        status = Status.PENDING;
        enrollmentForm = null;
    }
    public Applicant(ApplicationForm applicationForm){
        this.applicationForm = applicationForm;
        status = Status.PENDING;
        enrollmentForm = null;
    }

    public Status getStatus() {
        try{
            String sql = String.format(
                "select status from applicant " +
                "where applicant_id='%s'",applicationId
            );
            ResultSet resultSet = Database.executeQuery(sql);
            if(resultSet.next())
                status = Status.valueOf(resultSet.getString("status"));
        }catch (SQLException e){
            e.printStackTrace();
        }
        return status;
    }
    public ApplicationForm getApplicationForm(){
        return applicationForm;
    }
    public EnrollmentForm getEnrollmentForm() {
        try{
            String sql = String.format(
                "select * from enrollment_form " +
                "where applicant_id='%s'",applicationId
            );
            ResultSet resultSet = Database.executeQuery(sql);
            if(resultSet.next()){
                String form = resultSet.getString("form");
                String hsc_mark_sheet = resultSet.getString("hsc_mark_sheet");
                String entrance_mark_sheet = resultSet.getString("entrance_mark_sheet");
                enrollmentForm = new EnrollmentForm();
                enrollmentForm.setForm(form);
                enrollmentForm.setHscMarkSheet(hsc_mark_sheet);
                enrollmentForm.setEntranceMarkSheet(entrance_mark_sheet);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return enrollmentForm;
    }
    public String getApplicationId(){
        return applicationId;
    }
    protected String getPassword(){
        return password;
    }

    public String getEnrollmentId() {
        try{
            String sql = String.format(
                "select enrollment_id from applicant " +
                "where applicant_id='%s'",applicationId
            );
            ResultSet resultSet = Database.executeQuery(sql);
            if(resultSet.next())
                enrollmentId = resultSet.getString("enrollment_id");

        }catch (SQLException e){
            e.printStackTrace();
        }
        return enrollmentId;
    }

    public void setStatus(Status status){
        this.status = status;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void updatePassword(String password){
        this.password = password;
        String applicant_id = this.applicationId;
        String sql = String.format(
            "update applicant set password='%s' " +
            "where applicant_id='%s'",password,applicant_id
        );
        Database.executeUpdate(sql);
    }

    public void setEnrollmentId(String enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public void setApplicationForm(ApplicationForm applicationForm) {
        this.applicationForm = applicationForm;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public void setEnrollmentForm(EnrollmentForm enrollmentForm) {
        this.enrollmentForm = enrollmentForm;
        if (enrollmentForm!=null) {
            String sql = String.format(
                "update enrollment_form set form='%s', hsc_mark_sheet='%s', entrance_mark_sheet='%s' " +
                "where applicant_id='%s'",
                enrollmentForm.getForm(),enrollmentForm.getHscMarkSheet(),enrollmentForm.getEntranceMarkSheet(),applicationId
            );
            Database.executeUpdate(sql);
        }
    }


    public Status apply(){
        if(!applicationForm.isValid())
            return Status.FAILED;
        if(!University.isUnique(this))
            return Status.FAILED;
        applicationId = University.generateApplicationId();
        University.addApplicant(this);
        status = Status.APPLIED;
        commitStatus();
        return status;
    }


    public Status hover(){
        getStatus();
        if(status == Status.SHORTLISTED) {
            status = Status.FLOATED;
            commitStatus();
        }
        return status;
    }

    public Status lock(){
        getStatus();
        if(status==Status.SHORTLISTED || status==Status.FLOATED) {
            status = Status.LOCKED;
            commitStatus();
        }
        return status;
    }

    public boolean matchPassword(String password){
        return this.password.equals(password);
    }

    public void commitStatus(){
        String applicant_id = this.applicationId;
        String status = this.status.toString();

        String sql = String.format(
            "update applicant set status='%s' " +
            "where applicant_id='%s'",status,applicant_id
        );
        Database.executeUpdate(sql);
    }

    @Override
    public String toString() {
        if(applicationId==null)
            return status.toString();
        return applicationId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (this.getClass().equals(obj.getClass())){
            Applicant first = this;
            Applicant second = (Applicant) obj;
            if(first.applicationForm.getUniqueIdNo().equals(second.applicationForm.getUniqueIdNo()))
                return true;
            if(first.applicationForm.getEmail().equals(second.applicationForm.getEmail()))
                return true;
            if(first.applicationForm.getPhNo().equals(second.applicationForm.getPhNo()))
                return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return applicationId.hashCode();
    }

    @Override
    public int compareTo(Applicant that) {
        Examination exam1 = this.applicationForm.getExamination();
        Examination exam2 = that.applicationForm.getExamination();

        if(exam1.getPercentile()>exam2.getPercentile())
            return -1;
        else if(exam1.getPercentile()<exam2.getPercentile())
            return 1;
        else
            if(exam1.getObtainedMarks()>exam2.getObtainedMarks())
                return -1;
            else if(exam1.getObtainedMarks()<exam2.getObtainedMarks())
                return 1;
            else return (this.applicationId.compareTo(that.applicationId));
    }

    public static enum UniqueId{
        UIDAI,PASSPORT,PAN,DRIVING_LICENSE,VOTER_ID
    }

    public static enum Status{
        PENDING,APPLIED,SHORTLISTED,LOCKED,REJECTED,FLOATED,UNDER_VERIFICATION,ENROLLED,NOT_FOUND,FAILED,DISQUALIFIED
    }

    final static class Name implements Serializable{
        private static final long serialVersionUID = 73L;

        private String first;
        private String middle = "";
        private String last;

        public Name(String first,String last){
            this.first = first;
            this.last = last;
        }
        public Name(String first,String middle,String last){
            this(first,last);
            this.middle = middle;
        }

        public String getFirst() {
            return first;
        }
        public String getMiddle() {
            return middle;
        }
        public String getLast() {
            return last;
        }

        @Override
        public String toString() {
            if (middle.length()==0)
                return first + " " + last;
            return first + " " + middle + " " + last;
        }
    }
}
