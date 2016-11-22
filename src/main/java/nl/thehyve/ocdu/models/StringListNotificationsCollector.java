package nl.thehyve.ocdu.models;

import nl.thehyve.ocdu.models.errors.AbstractMessage;
import nl.thehyve.ocdu.models.errors.SubmissionResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jacob on 11/11/16.
 */
public class StringListNotificationsCollector implements NotificationsCollector {

    private List<AbstractMessage> notificationList;

    private String targetSystem;

    public StringListNotificationsCollector(String targetSystem) {
        notificationList = new ArrayList<>();
        this.targetSystem = targetSystem;
    }

    public List<AbstractMessage> getNotificationList() {
        return notificationList;
    }

    @Override
    public void addNotification(String notification) {
        SubmissionResult submissionResult = new SubmissionResult(notification);
        submissionResult.setError(false);
        notificationList.add(submissionResult);
    }

    @Override
    public String getTargetSystem() {
        return targetSystem;
    }
}
