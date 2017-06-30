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

package nl.thehyve.ocdu.validators.clinicalDataChecks;

import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OcDefinitions.CRFDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.EventDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.ItemDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.errors.CRFVersionMismatchError;
import nl.thehyve.ocdu.models.errors.ErrorClassification;
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

        String studyIdentifier = metaData.getProtocolName();

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
                    clinicalDataToUpload.addErrorClassification(ErrorClassification.BLOCK_ENTIRE_UPLOAD);
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
