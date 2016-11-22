package nl.thehyve.ocdu.models;

import nl.thehyve.ocdu.models.errors.MessageType;

/**
 * Should collect messages (error-messages, process progress messages etc.) for notifications to actors (an human
 * in case of the interactive mode or an email for the unattended mode)
 * Created by jacob on 11/10/16.
 */
public interface NotificationsCollector {

    /**
     * Add a notification for the actor.
     * @param notification
     */
    public void addNotification(String notification, MessageType messageType);

    /**
     * Should return the target system to which the data will be uploaded (e.g.
     * <code>"TraIT-OpenClinica production"</code>).
     * @return the target system
     */
    public String getTargetSystem();
}
