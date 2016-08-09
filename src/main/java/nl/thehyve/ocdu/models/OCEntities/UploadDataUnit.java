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
