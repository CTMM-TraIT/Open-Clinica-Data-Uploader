package nl.thehyve.ocdu.models.errors;

/**
 * Created by jacob on 8/5/16.
 */
public abstract class AbstractMessage {

    protected boolean isError = true; // Error by default
    protected String message;
    protected String subject;


    public AbstractMessage(String message) {
        this.message = message;
        this.subject = "";
    }

    public boolean isError() {
        return isError;
    }

    public void setError(boolean error) {
        isError = error;
    }

    public String getMessage() {
        return message;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
