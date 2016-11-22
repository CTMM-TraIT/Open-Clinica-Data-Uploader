package nl.thehyve.ocdu.models.errors;

/**
 * Created by jacob on 8/5/16.
 */
public class SubmissionResult extends AbstractMessage {

    public SubmissionResult(String message) {
        super(message);
        messageType = MessageType.NOTIFICATION;
    }
}
