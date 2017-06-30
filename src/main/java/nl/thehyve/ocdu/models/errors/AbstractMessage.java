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
