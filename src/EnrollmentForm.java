import java.io.Serializable;

public class EnrollmentForm implements Serializable {
    private static final long serialVersionUID = 73L;

    private String form = "EMPTY";
    private String hscMarkSheet = "EMPTY";
    private String entranceMarkSheet = "EMPTY";

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getHscMarkSheet() {
        return hscMarkSheet;
    }

    public void setHscMarkSheet(String hscMarkSheet) {
        this.hscMarkSheet = hscMarkSheet;
    }

    public String getEntranceMarkSheet() {
        return entranceMarkSheet;
    }

    public void setEntranceMarkSheet(String entranceMarkSheet) {
        this.entranceMarkSheet = entranceMarkSheet;
    }

    public boolean isValid(){
        if(hscMarkSheet.equalsIgnoreCase("EMPTY"))
            return false;
        if(form.equalsIgnoreCase("EMPTY"))
            return false;
        if(entranceMarkSheet.equalsIgnoreCase("EMPTY"))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "    {" +
                "\n\tform: '" + form + '\'' +
                ",\n\tHSC Mark Sheet: " + hscMarkSheet +
                ",\n\tEntrance MarkSheet: '" + entranceMarkSheet + '\'' +
                "\n    }";
    }
}

