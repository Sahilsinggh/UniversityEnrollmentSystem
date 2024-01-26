import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SessionHandler implements Runnable {
    private static final Map<String,Session> applicantSessions;
    private static final Map<String,Session> adminSessions;

    static {
        applicantSessions = new HashMap<>();
        adminSessions = new HashMap<>();
    }

    public static boolean isApplicantActive(String applicantId){
        return applicantSessions.containsKey(applicantId);
    }

    public static boolean isAdminActive(String username){
        return adminSessions.containsKey(username);
    }

    public static Session getOrCreateApplicantSession(String applicantId){
        if(isApplicantActive(applicantId))
            return applicantSessions.get(applicantId);

        applicantSessions.put(applicantId,new Session(applicantId));
        return applicantSessions.get(applicantId);
    }

    public static Session getOrCreateAdminSession(String username){
        if(isAdminActive(username))
            return adminSessions.get(username);

        adminSessions.put(username,new Session(username));
        return adminSessions.get(username);
    }

    public static void deleteApplicantSession(String applicantId){
        applicantSessions.remove(applicantId);
    }

    public static void deleteAdminSession(String username){
        adminSessions.remove(username);
    }

    @Override
    public void run() {
        for (String identifier : adminSessions.keySet()) {
            Session session = adminSessions.get(identifier);
            long inactiveTime = (new Date().getTime() - session.getLastActivity().getTime())/1000;
            if (inactiveTime>900) {
                System.out.println(session + " inactive");
                adminSessions.remove(identifier);
            }

        }

        for (String identifier : applicantSessions.keySet()) {
            Session session = applicantSessions.get(identifier);
            long inactiveTime = (new Date().getTime() - session.getLastActivity().getTime())/1000;
            if (inactiveTime>900) {
                System.out.println(session + " inactive");
                applicantSessions.remove(identifier);
            }
        }
    }
}
