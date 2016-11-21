package nl.thehyve.ocdu.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jacob on 11/11/16.
 */
public class StringListNotificationsCollector implements NotificationsCollector {

    private List<String> notificationList;

    private String targetSystem;

    public StringListNotificationsCollector(String targetSystem) {
        notificationList = new ArrayList<>();
        this.targetSystem = targetSystem;
    }

    public List<String> getNotificationList() {
        return notificationList;
    }

    @Override
    public void addNotification(String notification) {
        notificationList.add(notification);
    }

    @Override
    public String getTargetSystem() {
        return targetSystem;
    }
}
