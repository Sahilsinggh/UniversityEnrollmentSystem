import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Support {
    private String ticketNo;
    private String clientName;
    private String applicantId;
    private String adminUsername;
    private boolean resolved;

    public Support(){
        this.ticketNo = null;
        this.clientName = null;
        this.applicantId = null;
        this.adminUsername = null;
        resolved = false;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public void setTicketNo(String ticketNo) {
        this.ticketNo = ticketNo;
    }

    public boolean setApplicantId(String applicantId) {
        try{
            String sql = String.format(
                "select first_name,last_name from applicant natural join application_form " +
                "where applicant_id = '%s'",applicantId
            );
            ResultSet resultSet = Database.executeQuery(sql);
            if(resultSet.next()) {
                this.applicantId = applicantId;
                clientName = resultSet.getString("first_name");
                clientName += " ";
                clientName += resultSet.getString("last_name");
                return true;
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
        String sql = String.format(
            "update connection set username='%s' " +
            "where ticket_no='%s'",adminUsername,ticketNo
        );
        Database.executeUpdate(sql);
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
        String sql = String.format(
            "update connection set resolved=%b " +
            "where ticket_no='%s'",resolved,ticketNo
        );
        Database.executeUpdate(sql);
    }

    public String getTicketNo() {
        return ticketNo;
    }

    public String getClientName() {
        return clientName;
    }

    public String getApplicantId() {
        return applicantId;
    }

    public String getAdminUsername() {
        try {
            String sql = String.format("select username from connection where ticket_no='%s'",ticketNo);
            ResultSet resultSet = Database.executeQuery(sql);
            if(resultSet.next())
                adminUsername = resultSet.getString("username");
        }catch (SQLException e){
            e.printStackTrace();
        }
        return adminUsername;
    }

    public boolean isResolved() {
        try {
            String sql = String.format(
                "select resolved from connection " +
                "where ticket_no='%s'" ,ticketNo
            );
            ResultSet resultSet = Database.executeQuery(sql);
            if(resultSet.next())
                resolved =  resultSet.getBoolean("resolved");
        }catch (SQLException e){
            e.printStackTrace();
        }
        return resolved;
    }

    public void generateTicketNo(){
        try{
            int id = 0;
            ResultSet resultSet = Database.executeQuery("select count(*) from connection");
            if(resultSet.next())
                id = resultSet.getInt(1);

            Random random = new Random();
            long filler = 100000000 + random.nextInt(900000000);
            String prefix = Long.toHexString(filler);
            String suffix = Integer.toHexString(id+1);
            ticketNo = prefix+suffix;

        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public String getConversation(boolean admin){
        String conversation="";
        try {
            String sql = String.format(
                "select * from conversation " +
                "where ticket_no='%s' " +
                "order by sent_at", ticketNo
            );
            ResultSet resultSet = Database.executeQuery(sql);
            if(admin){
                while (resultSet.next()){
                    if(resultSet.getBoolean("sent_by_admin"))
                        conversation += "You: "+resultSet.getString("message")+"\n";
                    else
                        conversation += ANSI.CYAN+getClientName()+": "+resultSet.getString("message")+"\n"+ANSI.RESET;
                }
            }
            else {
                while (resultSet.next()){
                    if(resultSet.getBoolean("sent_by_admin"))
                        conversation += ANSI.CYAN+getAdminUsername()+": "+resultSet.getString("message")+"\n"+ANSI.RESET;
                    else
                        conversation += "You: "+resultSet.getString("message")+"\n";
                }
            }
            return conversation;
        }catch (SQLException e){
            e.printStackTrace();
            return null;
        }

    }

    public List<String> getConversation(){
        List<String> conversation= new ArrayList<>();
        try {
            String sql = String.format(
                    "select * from conversation " +
                            "where ticket_no='%s' " +
                            "order by sent_at", ticketNo
            );
            ResultSet resultSet = Database.executeQuery(sql);
            while (resultSet.next()){
                conversation.add(resultSet.getString("message"));
                conversation.add(resultSet.getString("sent_by_admin"));
            }
            return conversation;
        }catch (SQLException e){
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void post(String message,boolean admin){
        String sql = String.format(
            "insert into conversation (message, ticket_no, sent_by_admin) " +
            "value ('%s','%s',%b)",message,ticketNo,admin
        );
        Database.executeUpdate(sql);
    }
}
