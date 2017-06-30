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
