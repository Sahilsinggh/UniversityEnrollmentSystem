import java.sql.*;

public abstract class Database implements AutoCloseable{
    static Connection connection = null;

    static final String username = "31165";
    static final String password = "31165@mysql";
    static final String url = "jdbc:mysql://localhost:3306/UniversityEnrollmentSystem";

    static {
        try {
            connection = DriverManager.getConnection(url,username,password);
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    private Database(){
        System.out.println("private constructor");
    }

    public static ResultSet executeQuery(String sql){
        try {
            Statement statement = connection.createStatement();
            return statement.executeQuery(sql);
        }catch (SQLException e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static int executeUpdate(String sql){
        try{
            Statement statement = connection.createStatement();
            return statement.executeUpdate(sql);
        }catch (SQLException e){
            System.out.println(e.getMessage());
            return -1;
        }
    }

    public static boolean execute(String sql){
        try {
            Statement statement = connection.createStatement();
            return statement.execute(sql);
        }catch (SQLException e){
            System.out.println(e.getMessage());
            return false;
        }
    }

    public static void addApplicantObject(Applicant applicant){
        String sql;

        String hsc_reg_no = applicant.getApplicationForm().getHsc().getRegNo();
        String board = applicant.getApplicationForm().getHsc().getBoard();
        double percentage = applicant.getApplicationForm().getHsc().getPercentage();
        sql = String.format(
            "insert into hsc(hsc_reg_no, board, percentage) " +
            "values ('%s','%s',%f)",hsc_reg_no,board,percentage
        );
        executeUpdate(sql);

        String entrance_reg_no = applicant.getApplicationForm().getExamination().getRegNo();
        double percentile = applicant.getApplicationForm().getExamination().getPercentile();
        double score = applicant.getApplicationForm().getExamination().getObtainedMarks();
        sql = String.format(
           "insert into entrance(entrance_reg_no, percentile, score) " +
           "values ('%s',%f,%f)",entrance_reg_no,percentile,score
        );
        executeUpdate(sql);

        String unique_id = applicant.getApplicationForm().getUniqueIdNo();
        String first_name = applicant.getApplicationForm().getName().getFirst();
        String middle_name = applicant.getApplicationForm().getName().getMiddle();
        String last_name = applicant.getApplicationForm().getName().getLast();
        String email = applicant.getApplicationForm().getEmail();
        String phone = applicant.getApplicationForm().getPhNo();
        String branch_name = applicant.getApplicationForm().getBranchName();
        sql = String.format(
            "insert into application_form (unique_id, first_name, middle_name, last_name, email, phone, entrance_reg_no, hsc_reg_no, branch_name) " +
            "values ('%s','%s','%s','%s','%s','%s','%s','%s','%s')",unique_id,first_name,middle_name,last_name,email,phone,entrance_reg_no,hsc_reg_no,branch_name
        );
        executeUpdate(sql);

        String applicant_id = applicant.getApplicationId();
        String password = applicant.getPassword();
        Applicant.Status status = applicant.getStatus();
        sql = String.format(
            "insert into applicant (applicant_id, password, unique_id, status) " +
            "values ('%s','%s','%s','%s')",applicant_id,password,unique_id,status
        );
        executeUpdate(sql);

    }

    public static Applicant getApplicantObject(String id){
        try {
            String applicant_id, password, enrollment_id, unique_id, board, form, hsc_mark_sheet, entrance_mark_sheet;
            String first_name, middle_name, last_name, email, phone, entrance_reg_no, hsc_reg_no, branch_name;
            double percentage,percentile,score;
            Applicant.Status status;
            Applicant.UniqueId id_type;

            String sql = String.format("select * from applicant where applicant_id='%s'",id);
            ResultSet resultSet = executeQuery(sql);
            if(resultSet.next()){
                applicant_id = resultSet.getString("applicant_id");
                status = Applicant.Status.valueOf(resultSet.getString("status"));
                password = resultSet.getString("password");
                enrollment_id = resultSet.getString("enrollment_id");
                unique_id = resultSet.getString("unique_id");
            }
            else return null;

            sql = String.format("select * from application_form where unique_id='%s'",unique_id);
            resultSet = executeQuery(sql);
            if(resultSet.next()){
                id_type = Applicant.UniqueId.valueOf(resultSet.getString("id_type"));
                first_name = resultSet.getString("first_name");
                middle_name = resultSet.getString("middle_name");
                last_name = resultSet.getString("last_name");
                email = resultSet.getString("email");
                phone = resultSet.getString("phone");
                entrance_reg_no = resultSet.getString("entrance_reg_no");
                hsc_reg_no = resultSet.getString("hsc_reg_no");
                branch_name = resultSet.getString("branch_name");

            }
            else return null;

            sql = String.format("select * from hsc where hsc_reg_no='%s'",hsc_reg_no);
            resultSet = executeQuery(sql);
            if(resultSet.next()){
                board = resultSet.getString("board");
                percentage = resultSet.getDouble("percentage");
            }
            else return null;

            sql = String.format("select * from entrance where entrance_reg_no='%s'",entrance_reg_no);
            resultSet = executeQuery(sql);
            if(resultSet.next()){
                percentile = resultSet.getDouble("percentile");
                score = resultSet.getDouble("score");
            }
            else return null;

            Examination entrance = new Examination();
            entrance.setRegNo(entrance_reg_no);
            entrance.setPercentile(percentile);
            entrance.setObtainedMarks(score);

            ApplicationForm applicationForm = new ApplicationForm();
            applicationForm.setId(id_type,unique_id);
            applicationForm.setName(new Applicant.Name(first_name,middle_name,last_name));
            applicationForm.setEmail(email);
            applicationForm.setPhNo(phone);
            applicationForm.setExamination(entrance);
            applicationForm.setHsc(board,hsc_reg_no,percentage);
            applicationForm.setBranchName(branch_name);

            Applicant applicant = new Applicant();
            applicant.setApplicationId(applicant_id);
            applicant.setApplicationForm(applicationForm);
            applicant.setStatus(status);
            applicant.setPassword(password);
            applicant.setEnrollmentId(enrollment_id);
            applicant.setEnrollmentForm(null);

            //while issuing EnrollmentForm update enrollment_form with empty place holder
            sql = String.format("select * from enrollment_form where applicant_id='%s'",applicant_id);
            resultSet = executeQuery(sql);
            if(resultSet.next()) {
                EnrollmentForm enrollmentForm = new EnrollmentForm();
                form = resultSet.getString("form");
                hsc_mark_sheet = resultSet.getString("hsc_mark_sheet");
                entrance_mark_sheet = resultSet.getString("entrance_mark_sheet");
                enrollmentForm.setForm(form);
                enrollmentForm.setHscMarkSheet(hsc_mark_sheet);
                enrollmentForm.setEntranceMarkSheet(entrance_mark_sheet);
                applicant.setEnrollmentForm(enrollmentForm);
            }
            return applicant;
        }catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }
    
    public static Support getSupportObject(String ticketNo){
        try {
            String sql = String.format(
                "select * from connection " +
                 "where ticket_no='%s'",ticketNo
            );
            ResultSet resultSet = executeQuery(sql);
            if(resultSet.next()){
                Support support = new Support();
                String ticket_no = resultSet.getString("ticket_no");
                String applicant_id = resultSet.getString("applicant_id");
                String client_name = resultSet.getString("client_name");
                String username = resultSet.getString("username");
                boolean resolved = resultSet.getBoolean("resolved");

                support.setTicketNo(ticket_no);
                if(username != null)
                    support.setAdminUsername(username);
                support.setClientName(client_name);
                support.setResolved(resolved);
                if(applicant_id != null)
                    support.setApplicantId(applicant_id);
                return support;
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    public static void addSupportObject(Support support){
        String ticket_no = support.getTicketNo();
        String applicant_id = support.getApplicantId();
        String client_name = support.getClientName();
        String username = support.getAdminUsername();
        boolean resolved = support.isResolved();
        String sql = "";
        if(applicant_id == null)
            sql = String.format(
                "insert into connection (ticket_no, client_name) " +
                "values ('%s','%s')",ticket_no,client_name,applicant_id
            );
        else
            sql = String.format(
                "insert into connection (ticket_no, client_name, applicant_id) " +
                "values ('%s','%s','%s')",ticket_no,client_name,applicant_id
            );
        executeUpdate(sql);
    }

    public static void main(String[] args) {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select version()");
            System.out.println("Database connected");
            while(resultSet.next())
                System.out.println("MySQL: "+resultSet.getString(1));
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }
}
