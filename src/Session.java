import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Session {
    private final String identifier;
    private final Lock lock;
    private  Date lastActivity;

    public Session(){
        this.identifier = null;
        lock = new ReentrantLock();
        lastActivity = new Date();
    }

    public Session(String identifier) {
        this.identifier = identifier;
        lock = new ReentrantLock();
        lastActivity = new Date();
    }

    public String getIdentifier() {
        return identifier;
    }

    public Lock getLock() {
        return lock;
    }

    public Date getLastActivity() {
        return lastActivity;
    }

    public void updateLastActivity(){
        lastActivity = new Date();
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(this.getClass().equals(obj.getClass())){
            Session first = this;
            Session second = (Session) obj;
            return first.identifier.equals(second.identifier);
        }
        return false;
    }

    @Override
    public String toString() {
        return identifier;
    }
}
