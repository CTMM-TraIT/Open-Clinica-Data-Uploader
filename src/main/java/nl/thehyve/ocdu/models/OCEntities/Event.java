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

package nl.thehyve.ocdu.models.OCEntities;

import nl.thehyve.ocdu.models.OcUser;
import nl.thehyve.ocdu.models.UploadSession;
import nl.thehyve.ocdu.models.errors.ErrorClassification;
import nl.thehyve.ocdu.validators.UtilChecks;
import org.apache.commons.lang3.StringUtils;
import org.openclinica.ws.beans.EventType;
import org.openclinica.ws.beans.SiteRefType;
import org.openclinica.ws.beans.StudyRefType;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import javax.persistence.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents user-submitted event-subject pair. Meant to store data as-is.
 * Created by piotrzakrzewski on 16/04/16.
 */

@Entity
public class Event implements OcEntity, UserSubmitted, EventReference {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @ManyToOne
    private UploadSession submission;
    @ManyToOne
    private OcUser owner;
    private String studyProtocolName;


    @ElementCollection(targetClass=ErrorClassification.class)
    @Enumerated(EnumType.ORDINAL)
    @CollectionTable(name="event_errors", joinColumns = {@JoinColumn(name="id")})
    private Set<ErrorClassification> errorClassificationSet;

    private String eventName;
    private String ssid;
    private String study;
    private String location;
    private String site;
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;
    private String repeatNumber;

    /**
     * The line number in the file containing the event definitions. Is needed for error-reporting
     */
    private long lineNumber;


    public Event() {
        this.errorClassificationSet = new HashSet<>();
    }


    @Override
    public UploadSession getSubmission() {
        return submission;
    }

    @Override
    public void setSubmission(UploadSession submission) {
        this.submission = submission;
    }

    @Override
    public OcUser getOwner() {
        return owner;
    }

    @Override
    public void setOwner(OcUser owner) {
        this.owner = owner;
    }

    @Override
    public String getEventName() {
        return eventName;
    }

    @Override
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    @Override
    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getStudyProtocolName() {
        return studyProtocolName;
    }

    public void setStudyProtocolName(String studyProtocolName) {
        this.studyProtocolName = studyProtocolName;
    }

    @Override
    public String getStudy() {
        return study;
    }

    public void setStudy(String study) {
        this.study = study;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getRepeatNumber() {
        return repeatNumber;
    }

    public void setRepeatNumber(String repeatNumber) {
        this.repeatNumber = repeatNumber;
    }

    public long getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(long lineNumber) {
        this.lineNumber = lineNumber;
    }

    @Override
    public String toString() {
        return "Event{" +
                "error= '" + Arrays.toString(errorClassificationSet.toArray()) + '\'' +
                ", repeatNumber=" + repeatNumber +
                ", eventName='" + eventName + '\'' +
                ", ssid='" + ssid + '\'' +
                ", study='" + study + '\'' +
                ", location='" + location + '\'' +
                ", site='" + site + '\'' +
                ", startDate=" + startDate +
                ", startTime=" + startTime +
                ", endDate=" + endDate +
                ", endTime=" + endTime +
                ", lineNumber=" + lineNumber +
                '}';
    }

    /**
     * creates a key to filter a list for all events present in a list of {@link ClinicalData}.
     * @return a key uniquely identifying an event
     */
    public String createEventKey(String eventOID) {
        StringBuffer ret = new StringBuffer();
        ret.append(study);
        if (site != null) {
            ret.append(site);
        }
        ret.append(ssid);
        ret.append(eventOID);
        ret.append(repeatNumber);
        return ret.toString().toUpperCase();
    }

    /**
     * Returns a reference to the instance <code>this</code>. For use in lambda-expressions. See also the Java Language
     * Specification section 15.27.2.
     * @return
     */
    public Event referenceToSelf() {
        return this;
    }



    public String createEventKey() {
        StringBuffer ret = new StringBuffer();
        ret.append(UtilChecks.nullSafeToUpperCase(study));
        ret.append(ClinicalData.KEY_SEPARATOR);
        ret.append(UtilChecks.nullSafeToUpperCase(site));
        ret.append(ClinicalData.KEY_SEPARATOR);
        ret.append(UtilChecks.nullSafeToUpperCase(ssid));
        ret.append(ClinicalData.KEY_SEPARATOR);
        ret.append(UtilChecks.nullSafeToUpperCase(eventName));
        ret.append(ClinicalData.KEY_SEPARATOR);
        ret.append(UtilChecks.nullSafeToUpperCase(repeatNumber));
        return ret.toString().toUpperCase();
    }

    public EventType createEventType(Map<String, String> eventNameOIDMap) {
        EventType ret = new EventType();
        StudyRefType studyRefType = new StudyRefType();
        studyRefType.setIdentifier(studyProtocolName);
        SiteRefType siteRefType = new SiteRefType();
        siteRefType.setIdentifier(site);
        studyRefType.setSiteRef(siteRefType);
        ret.setStudyRef(studyRefType);
        String eventOID = eventNameOIDMap.get(eventName);
        if (StringUtils.isEmpty(eventOID)) {
            throw new IllegalStateException("No eventOID found for the event with name " + eventName);
        }
        ret.setEventDefinitionOID(eventName);
        return ret;
    }

    public ClinicalData createClinicaData() {
        ClinicalData clinicalData = new ClinicalData();
        clinicalData.setStudy(study);
        clinicalData.setSsid(ssid);;
        clinicalData.setSite(site);
        clinicalData.setEventName(eventName);
        clinicalData.setEventRepeat(repeatNumber);
        return clinicalData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        if (eventName != null ? !eventName.equals(event.eventName) : event.eventName != null) return false;
        if (ssid != null ? !ssid.equals(event.ssid) : event.ssid != null) return false;
        return repeatNumber != null ? repeatNumber.equals(event.repeatNumber) : event.repeatNumber == null;

    }

    @Override
    public int hashCode() {
        int result = eventName != null ? eventName.hashCode() : 0;
        result = 31 * result + (ssid != null ? ssid.hashCode() : 0);
        result = 31 * result + (repeatNumber != null ? repeatNumber.hashCode() : 0);
        return result;
    }

    public boolean hasErrorOfType(ErrorClassification errorClassification) {
        return errorClassificationSet.contains(errorClassification);
    }

    public void addErrorClassification(ErrorClassification errorClassification) {
        errorClassificationSet.add(errorClassification);
    }
}
