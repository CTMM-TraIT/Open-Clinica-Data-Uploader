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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * AKA User Submission. Stores all user-submitted data along with current submission state
 * (step in the workflow)
 *
 * Created by piotrzakrzewski on 28/04/16.
 */
@Entity
public class UploadSession {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;

    @ManyToOne()
    private OcUser owner;

    public enum Step {
        MAPPING, FEEDBACK_DATA, SUBJECTS, FEEDBACK_SUBJECTS, EVENTS, FEEDBACK_EVENTS, PRE_ODM_UPLOAD, ODM_UPLOAD, FINAL
    }

    private Step step;
    private Date savedDate;
    private String study;

    private boolean uponNotStarted = true;
    private boolean uponDataEntryStarted;
    private boolean uponDataEntryCompleted;
    private String crfStatusAfterUpload;


    public String getStudy() {
        return study;
    }

    public void setStudy(String study) {
        this.study = study;
    }

    public UploadSession() {
        // needed by hibernate
    }

    public UploadSession(String name, Step step, Date savedDate, OcUser owner) {
        this.name = name;
        this.step = step;
        this.savedDate = savedDate;
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Step getStep() {
        return step;
    }

    public void setStep(Step step) {
        this.step = step;
    }

    public Date getSavedDate() {
        return savedDate;
    }

    public void setSavedDate(Date savedDate) {
        this.savedDate = savedDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public OcUser getOwner() {
        return owner;
    }

    public boolean isUponNotStarted() {
        return uponNotStarted;
    }

    public void setUponNotStarted(boolean uponNotStarted) {
        this.uponNotStarted = uponNotStarted;
    }

    public boolean isUponDataEntryStarted() {
        return uponDataEntryStarted;
    }

    public void setUponDataEntryStarted(boolean uponDataEntryStarted) {
        this.uponDataEntryStarted = uponDataEntryStarted;
    }

    public boolean isUponDataEntryCompleted() {
        return uponDataEntryCompleted;
    }

    public void setUponDataEntryCompleted(boolean uponDataEntryCompleted) {
        this.uponDataEntryCompleted = uponDataEntryCompleted;
    }

    public void setOwner(OcUser owner) {
        this.owner = owner;
    }

    public String getCrfStatusAfterUpload() {
        return crfStatusAfterUpload;
    }

    public void setCrfStatusAfterUpload(String crfStatusAfterUpload) {
        this.crfStatusAfterUpload = crfStatusAfterUpload;
    }
}
