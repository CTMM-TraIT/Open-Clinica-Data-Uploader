package nl.thehyve.ocdu.validators.clinicalDataChecks;

import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OcDefinitions.CRFDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.EventDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.ItemDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.errors.CRFVersionMismatchError;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import org.openclinica.ws.beans.EventCrfInformationList;
import org.openclinica.ws.beans.EventCrfType;
import org.openclinica.ws.beans.EventResponseType;
import org.openclinica.ws.beans.EventsType;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Checks if the CRF-version as specified in the input, matches the CRF-version of existing data for each subject
 * registered in OpenClinica.
 *
 * Created by jacob on 6/2/16.
 */
public class CRFVersionMatchCrossCheck implements ClinicalDataCrossCheck {

    @Override
    public ValidationErrorMessage getCorrespondingError(List<ClinicalData> data, MetaData metaData, Map<ClinicalData, ItemDefinition> itemDefMap, List<StudySubjectWithEventsType> subjectWithEventsTypeList, Map<ClinicalData, Boolean> shownMap, Map<String, Set<CRFDefinition>> eventMap) {
        // Assumption is that there is only 1 event and 1 CRF in a data file and that the clincalDataList only contains a single subjectID

        String studyIdentifier = metaData.getStudyName();

        Map<String, String> eventOIDNameMap =
                metaData.getEventDefinitions().stream().collect(Collectors.toMap(EventDefinition::getStudyEventOID, EventDefinition::getName));

        List<String> offendingNames = new ArrayList<>();
        for (ClinicalData clinicalDataToUpload : data) {
            String subjectLabel = clinicalDataToUpload.getSsid();
            List<ClinicalData> clinicalDataPresentInStudy = convertToClinicalData(subjectWithEventsTypeList, subjectLabel, studyIdentifier, eventOIDNameMap);
            for (ClinicalData clinicalDataInStudy : clinicalDataPresentInStudy) {
                if (clinicalDataInStudy.isSameCRF(clinicalDataToUpload) &&
                        (! (clinicalDataInStudy.hasSameCRFVersion(clinicalDataToUpload)))) {
                    String msg = "Subject " + subjectLabel + " has a mismatching CRF version (" +
                            clinicalDataToUpload.getCrfVersion()
                            + ") for CRF "
                            + clinicalDataInStudy.getCrfName()
                            + " in event " + clinicalDataInStudy.getEventName()
                            + ", repeat number " + clinicalDataToUpload.getEventRepeat();
                    if (!offendingNames.contains(msg)) {
                        offendingNames.add(msg);
                    }
                }
            }
        }

        if (offendingNames.isEmpty()) {
            return null;
        }
        CRFVersionMismatchError crfVersionMismatchError = new CRFVersionMismatchError();
        crfVersionMismatchError.addAllOffendingValues(offendingNames);
        return crfVersionMismatchError;

    }

    private List<ClinicalData> convertToClinicalData(List<StudySubjectWithEventsType> subjectWithEventsTypeList, String studySubjectLabel, String studyIdentifier, Map<String, String> eventOIDNameMap) {
        // TODO convert to lambda expressions ????
        List<ClinicalData> ret = new ArrayList<>();
        for (StudySubjectWithEventsType subjectWithEventsType : subjectWithEventsTypeList) {
            if (studySubjectLabel.equals(subjectWithEventsType.getLabel())) {
                EventsType eventsType = subjectWithEventsType.getEvents();
                for (EventResponseType eventResponseType : eventsType.getEvent()) {
                    String eventOID = eventResponseType.getEventDefinitionOID();
                    String eventName = eventOIDNameMap.get(eventOID);
                    String eventOrdinal = eventResponseType.getOccurrence();
                    for (EventCrfInformationList eventCrfInformationList : eventResponseType.getEventCrfInformation()) {
                        List<EventCrfType>  eventCrfTypeList = eventCrfInformationList.getEventCrf();
                        for (EventCrfType eventCrfType : eventCrfTypeList) {
                            ClinicalData clinicalData = new ClinicalData(1, studyIdentifier,
                                    null,
                                    subjectWithEventsType.getLabel(),
                                    null,
                                    eventName,
                                    eventOrdinal,
                                    eventCrfType.getName(),
                                    null,
                                    eventCrfType.getVersion(),
                                    null,
                                    null,
                                    null);
                            ret.add(clinicalData);
                        }
                    }
                }
            }
        }
        return ret;
    }
}
