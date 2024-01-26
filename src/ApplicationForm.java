import java.io.Serializable;

public class ApplicationForm implements Serializable {
    private static final long serialVersionUID = 73L;

    private Applicant.Name name;
    private Applicant.UniqueId id;
    private String uniqueIdNo;
    private HSC hsc;
    private Examination examination;
    private String email;
    private String phNo;
    private String branchName;



    public void setName(Applicant.Name name){
        this.name = name;
    }
    public void setId(Applicant.UniqueId id,String uniqueIdNo){
        this.id = id;
        this.uniqueIdNo = uniqueIdNo;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setHsc(String board,String regNo,double percentage){
        this.hsc = new HSC(board,regNo,percentage);
    }
    public void setPhNo(String phNo) {
        this.phNo = phNo;
    }
    public void setBranchName(String branchName){
        this.branchName = branchName;
    }
    public void setExamination(Examination examination){
        this.examination = examination;
    }


    public Applicant.Name getName() {
        return name;
    }
    public Applicant.UniqueId getId() {
        return id;
    }
    public String getUniqueIdNo() {
        return uniqueIdNo;
    }
    public Examination getExamination() {
        return examination;
    }
    public String getEmail() {
        return email;
    }
    public String getPhNo() {
        return phNo;
    }
    public String getBranchName() {
        return branchName;
    }
    public HSC getHsc() {
        return hsc;
    }

    @Override
    public String toString() {
        return name.toString();
    }


    public boolean isValid(){
        if(name == null)        return false;
        if(id == null)          return false;
        if(uniqueIdNo == null)  return false;
        if(examination == null) return false;
        if(email == null)       return false;
        if(phNo == null)        return false;
        if(hsc == null)         return false;
        if(branchName == null)      return false;
        return true;
    }

    public static class HSC extends Examination implements Serializable{
        private static final long serialVersionUID = 73L;

        private String board;
        private double percentage;

        public HSC(String board,String regNo,double percentage){
            super("HSC",0);
            super.setRegNo(regNo);
            this.board = board;
            this.percentage = percentage;
        }

        public double getPercentage() {
            return percentage;
        }
        public String getBoard() {
            return board;
        }
    }

}
