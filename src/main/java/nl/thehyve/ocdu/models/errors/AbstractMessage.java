package nl.thehyve.ocdu.models.errors;

/**
 * Created by jacob on 8/5/16.
 */
public abstract class AbstractMessage {

    protected boolean isError = true; // Error by default
    protected MessageType messageType;
    protected String message;
    protected String subject;
    protected long lineNumber;


    public AbstractMessage(String message) {
        this.message = message;
        this.subject = "";
        this.messageType = MessageType.ERROR;
    }

    public boolean isError() {
        return messageType == MessageType.ERROR;
    }

    public boolean isWarning() {
        return messageType == MessageType.WARNING;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public boolean isNotification() {
        return messageType == MessageType.NOTIFICATION;
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

    public long getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(long lineNumber) {
        this.lineNumber = lineNumber;
    }
}
