import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class University {

    protected static Set<Branch> branches;
    public static String entrance;
    public static double maxMarks;
    public static String email;
    public static String contact;
    private static String sql;

    static {
        entrance = "JEE";
        maxMarks = 360;
        email = "heptadecane@gmail.com";
        contact = "9999999999";
        branches = new HashSet<>();
        branches.add(new Branch("COMP",300,300));
        branches.add(new Branch("ENTC",150,250));
        branches.add(new Branch("CHEM",200,200));
        branches.add(new Branch("CIVIL",200,180));

        //syncSeatData();
        for(Branch branch : branches){
            try {
                sql = String.format(
                    "select count(applicant_id) from applicant natural join application_form " +
                    "where status in ('SHORTLISTED','FLOATED','LOCKED','UNDER_VERIFICATION','ENROLLED') " +
                    "and branch_name='%s'",branch.name
                );
                ResultSet resultSet = Database.executeQuery(sql);
                if(resultSet.next())
                    branch.lockedSeats = resultSet.getInt(1);

                sql = String.format(
                    "select count(applicant_id) from applicant natural join application_form " +
                    "where status='ENROLLED' and branch_name='%s'",branch.name
                );
                resultSet = Database.executeQuery(sql);
                if(resultSet.next())
                    branch.allocatedSeats = resultSet.getInt(1);
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
    }

    public static boolean isUnique(Applicant newApplicant){
        String unique_id = newApplicant.getApplicationForm().getUniqueIdNo();
        String email = newApplicant.getApplicationForm().getEmail();
        String phone = newApplicant.getApplicationForm().getPhNo();
        String entrance_reg_no = newApplicant.getApplicationForm().getExamination().getRegNo();
        String hsc_reg_no = newApplicant.getApplicationForm().getHsc().getRegNo();

        try{
            sql = String.format(
                "select count(*) from application_form " +
                "where unique_id='%s' " +
                "or email='%s' " +
                "or phone='%s' " +
                "or entrance_reg_no='%s' " +
                "or hsc_reg_no='%s'",
                unique_id,email,phone,entrance_reg_no,hsc_reg_no
            );
            ResultSet resultSet = Database.executeQuery(sql);
            if(resultSet.next())
                return resultSet.getInt(1)==0;

        }catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public static Set<Branch> getBranches(){
        return Collections.unmodifiableSet(branches);
    }

    protected static Branch getBranch(String branchName){
        for(Branch branch:branches)
            if(branch.getName().equals(branchName))
                return branch;
        return null;
    }

    public static void addBranch(Branch branch){
        branches.add(branch);
    }

    public static String generateApplicationId() {
        Date date = Calendar.getInstance().getTime();
        String prefix = new SimpleDateFormat("ddMMyyyy").format(date);
        int id = 1;
        try {
            ResultSet resultSet = Database.executeQuery("select count(applicant_id) from applicant");
            resultSet.next();
            id = resultSet.getInt(1) + 1;
        }catch (SQLException e){
            e.printStackTrace();
        }

        String suffix = String.format("%05d",id);
        return "I"+prefix+suffix;
    }

    public static String generateEnrollmentId(Branch branch){
        Date date = Calendar.getInstance().getTime();
        String prefix = new SimpleDateFormat("yy").format(date);
        int id = 1;
        try {
            sql = String.format(
                "select count(applicant_id) from applicant natural join application_form "+
                "where status='ENROLLED' and branch_name='%s'",branch.name
            );
            ResultSet resultSet = Database.executeQuery(sql);
            resultSet.next();
            id = resultSet.getInt(1) + 1;
        }catch (SQLException e){
            e.printStackTrace();
        }
        String suffix = String.format("%03d",id);
        return branch.getName()+"2K"+prefix+suffix;
    }

    public static boolean addApplicant(Applicant applicant){
        if(applicant.getApplicationId()==null)
            return false;
        applicant.setStatus(Applicant.Status.APPLIED);
        Database.addApplicantObject(applicant);
        return false;
    }

    public static Applicant fetchApplicant(String id,String password){
        Applicant applicant = Database.getApplicantObject(id);
        if(applicant==null)
            return null;
        if(applicant.matchPassword(password))
            return applicant;
        else return null;
    }

    public static List<String> getEvents(){
        List<String> result = new ArrayList<>();
        try {
            ResultSet resultSet = Database.executeQuery("select * from event order by commencement");
            while (resultSet.next()){
                result.add(resultSet.getString("description"));
                result.add(resultSet.getString("commencement"));
            }
            return result;
        }catch (SQLException e){
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void addEvent(String event, String date){
        sql = String.format(
            "insert into event (description,commencement) values " +
            "('%s', '%s')",event,date
        );
        Database.executeUpdate(sql);
    }

    public static class Branch implements Serializable {
        private static final long serialVersionUID = 73L;

        String name;
        int seats;
        int lockedSeats;
        int allocatedSeats;
        int cutOff;

        public Branch(String name,int seats,int cutOff){
            this.name = name;
            this.seats = seats;
            lockedSeats = 0;
            allocatedSeats = 0;
            this.cutOff = cutOff;
        }

        public Branch(String name){
            this.name = name;
            this.seats = 0;
            lockedSeats = 0;
            allocatedSeats = 0;
            this.cutOff = 0;
        }

        public String getName() {
            return name;
        }public int getCutOff() {
            return cutOff;
        }

        protected void lockSeat(){
            if(seats > lockedSeats)
                lockedSeats++;
        }
        protected void unlockSeat(){
            if(lockedSeats > 0)
                lockedSeats--;
        }
        protected void allocateSeat(){
            if(lockedSeats > allocatedSeats)
                allocatedSeats++;
        }

        @Override
        public boolean equals(Object obj) {
            if(this == obj)
                return true;
            if(this.getClass().equals(obj.getClass())){
                Branch first = this;
                Branch second = (Branch) obj;
                return first.name.equals(second.name);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
