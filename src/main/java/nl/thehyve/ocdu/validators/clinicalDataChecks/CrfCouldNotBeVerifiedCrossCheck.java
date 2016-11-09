package nl.thehyve.ocdu.validators.clinicalDataChecks;

import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OcDefinitions.CRFDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.ItemDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.errors.CrfCouldNotBeVerified;
import nl.thehyve.ocdu.models.errors.ErrorClassification;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.validators.UtilChecks;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by piotrzakrzewski on 10/05/16.
 */
public class CrfCouldNotBeVerifiedCrossCheck implements ClinicalDataCrossCheck {


    private List<ClinicalData> getcrfCouldNotBeVerifiedOffenders(List<ClinicalData> data, Map<String, Set<CRFDefinition>> eventMap) {
        return data.stream().filter(clinicalData -> {
            Set<CRFDefinition> valid = eventMap.get(clinicalData.getEventName());
            if (valid == null) return true;
            else return false;
        }).collect(Collectors.toList());
    }

    @Override
    public ValidationErrorMessage getCorrespondingError(List<ClinicalData> data, MetaData metaData, Map<ClinicalData, ItemDefinition> itemDefMap, List<StudySubjectWithEventsType> studySubjectWithEventsTypeList, Map<ClinicalData, Boolean> shownMap, Map<String, Set<CRFDefinition>> eventMap) {
        List<ClinicalData> crfCouldNotBeVerifiedOffenders = getcrfCouldNotBeVerifiedOffenders(data, eventMap);
        if (crfCouldNotBeVerifiedOffenders.size() > 0) {
            CrfCouldNotBeVerified error = new CrfCouldNotBeVerified();
            Set<String> offendingNames = new HashSet<>();
            Set<String> offendingSubjectIDs = new HashSet<>();
            crfCouldNotBeVerifiedOffenders.stream().forEach(clinicalData -> {
                String crf = clinicalData.getCrfName();
                offendingNames.add(crf);
                offendingSubjectIDs.add(clinicalData.getSsid());
            });
            error.addAllOffendingValues(offendingNames);

            UtilChecks.addErrorClassificationForSubjects(data, offendingSubjectIDs, ErrorClassification.BLOCK_ENTIRE_CRF);
            return error;
        } else return null;
    }

}
