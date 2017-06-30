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

import nl.thehyve.ocdu.models.OcDefinitions.ODMElement;
import nl.thehyve.ocdu.models.OcUser;
import nl.thehyve.ocdu.models.UploadSession;
import nl.thehyve.ocdu.models.errors.ErrorClassification;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Represents user-submitted Subject information. Meant to store data as-is.
 *
 * Created by piotrzakrzewski on 16/04/16.
 */

@Entity
public class Subject implements OcEntity, UserSubmitted, ODMElement {

    private String ssid;
    private String gender;
    private String dateOfBirth;
    @Column(columnDefinition = "TEXT")
    private String personId;
    private String dateOfEnrollment;
    @Column(columnDefinition = "TEXT")
    private String secondaryId;
    @Column(columnDefinition = "TEXT")
    private String study;
    @Column(columnDefinition = "TEXT")
    private String site;

    @ElementCollection(targetClass=ErrorClassification.class)
    @Enumerated(EnumType.ORDINAL)
    @CollectionTable(name="subject_errors", joinColumns = {@JoinColumn(name="id")})
    private Set<ErrorClassification> errorClassificationSet;

    private String studyProtocolName; // AKA ProtocolName. To be used for registration, not taken from the user file

    @ManyToOne()
    private OcUser owner;
    @ManyToOne()
    private UploadSession submission;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    public Subject() {
        this.errorClassificationSet = new HashSet<>();
    }


    public OcUser getOwner() {
        return owner;
    }

    public UploadSession getSubmission() {
        return submission;
    }

    @Override
    public void setOwner(OcUser owner) {
        this.owner = owner;
    }

    @Override
    public void setSubmission(UploadSession submission) {
        this.submission = submission;
    }

    public long getId() {
        return id;
    }

    @Override
    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getStudyProtocolName() {
        return studyProtocolName;
    }

    public void setStudyProtocolName(String studyProtocolName) {
        this.studyProtocolName = studyProtocolName;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getDateOfEnrollment() {
        return dateOfEnrollment;
    }

    public void setDateOfEnrollment(String dateOfEnrollment) {
        this.dateOfEnrollment = dateOfEnrollment;
    }

    public String getSecondaryId() {
        return secondaryId;
    }

    public void setSecondaryId(String secondaryId) {
        this.secondaryId = secondaryId;
    }

    @Override
    public String getStudy() {
        return study;
    }

    public void setStudy(String study) {
        this.study = study;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    /**
     * Adds any missing leading zeros to the month and day of month in both the
     * {@link #dateOfBirth} and {@link #dateOfEnrollment}.
     */
    public void addLeadingZerosInDates() {
        dateOfEnrollment = fixLeadingZero(dateOfEnrollment);
        dateOfBirth = fixLeadingZero(dateOfBirth);
    }

    private String fixLeadingZero(String dateValue) {
        if (StringUtils.isEmpty(dateValue)) {
            return dateValue;
        }
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        dateFormat.setLenient(false);
        Date date;
        try {
            date = dateFormat.parse(dateValue);
        }
        catch (ParseException pe) {
            return dateValue;
        }
        return dateFormat.format(date);
    }

    @Override
    public String toString() {
        return "Subject{" +
                "study='" + study + '\'' +
                ", ssid='" + ssid + '\'' +
                ", gender='" + gender + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", personId='" + personId + '\'' +
                ", dateOfEnrollment=" + dateOfEnrollment +
                ", secondaryId='" + secondaryId + '\'' +
                '}';
    }

    public void appendODMStart(StringBuffer odmStringBuffer) {
        odmStringBuffer.append("<SubjectData SubjectKey=\"");
        odmStringBuffer.append(ssid);
        odmStringBuffer.append("\">");
        odmStringBuffer.append("</SubjectData>");
    }

    public void appendODMClose(StringBuffer odmStringBuffer) {
        odmStringBuffer.append("</SubjectData>");
    }

    public boolean hasErrorOfType(ErrorClassification errorClassification) {
        return errorClassificationSet.contains(errorClassification);
    }

    public void addErrorClassification(ErrorClassification errorClassification) {
        errorClassificationSet.add(errorClassification);
    }
}
