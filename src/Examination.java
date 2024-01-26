import java.io.Serializable;

public class Examination implements Serializable {
    private static final long serialVersionUID = 73L;

    private String title;
    private String regNo;
    private double maxMarks;
    private double obtainedMarks;
    private double percentile;

    public Examination(String title,double maxMarks){
        this.title = title;
        this.maxMarks = maxMarks;
    }
    public Examination(){
        this.title = "JEE";
        this.maxMarks = 360;
    }

    public String getTitle() {
        return title;
    }
    public String getRegNo() {
        return regNo;
    }
    public double getMaxMarks() {
        return maxMarks;
    }
    public double getObtainedMarks() {
        return obtainedMarks;
    }
    public double getPercentile() {
        return percentile;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }
    public void setMaxMarks(double maxMarks) {
        this.maxMarks = maxMarks;
    }
    public void setObtainedMarks(double obtainedMarks) {
        this.obtainedMarks = obtainedMarks;
    }
    public void setPercentile(double percentile) {
        this.percentile = percentile;
    }

    @Override
    public String toString() {
        return title;
    }

    @Override
    public int hashCode() {
        return title.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (this.getClass().equals(obj.getClass())){
            Examination first = this;
            Examination second = (Examination) obj;
            return first.title.equals(second.title);
        }
        return false;
    }


}
