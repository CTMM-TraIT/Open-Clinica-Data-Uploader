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

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper object containing all the required data for a single subject: the subject itself, the events and the
 * {@link ClinicalData}. Used a unit of work for the uploads.
 * Created by jacob on 8/8/16.
 */
public class UploadDataUnit {

    private Subject subject;
    private List<Event> eventList;
    private List<ClinicalData> clinicalDataList;
    private boolean subjectRegisteredInOpenClinica;

    public UploadDataUnit(Subject subject, boolean isSubjectRegisteredInOpenClinica) {
        this.subject = subject;
        this.subjectRegisteredInOpenClinica = isSubjectRegisteredInOpenClinica;
        this.eventList = new ArrayList<>();
        this.clinicalDataList = new ArrayList<>();
    };

    public void addEvent(Event event) {
        eventList.add(event);
    }

    public void addClinicalData(ClinicalData clinicalData) {
        this.clinicalDataList.add(clinicalData);
    }

    public Subject getSubject() {
        return subject;
    }

    public boolean isSubjectRegisteredInOpenClinica() {
        return subjectRegisteredInOpenClinica;
    }

    public List<Event> getEventList() {
        return eventList;
    }

    public List<ClinicalData> getClinicalDataList() {
        return clinicalDataList;
    }
}
