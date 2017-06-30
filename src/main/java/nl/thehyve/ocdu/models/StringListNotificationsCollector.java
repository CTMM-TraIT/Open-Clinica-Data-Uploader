/*
 * Copyright © 2016-2017 The Hyve B.V. and Netherlands Cancer Institute (NKI).
 *
 * This file is part of OCDI (OpenClinica Data Importer).
 *
 * OCDI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OCDI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OCDI. If not, see <http://www.gnu.org/licenses/>.
 */

package nl.thehyve.ocdu.models;

import nl.thehyve.ocdu.models.errors.AbstractMessage;
import nl.thehyve.ocdu.models.errors.MessageType;
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
    public void addNotification(String notification, MessageType messageType) {
        SubmissionResult submissionResult = new SubmissionResult(notification);
        submissionResult.setMessageType(messageType);
        notificationList.add(submissionResult);
    }

    @Override
    public String getTargetSystem() {
        return targetSystem;
    }
}
