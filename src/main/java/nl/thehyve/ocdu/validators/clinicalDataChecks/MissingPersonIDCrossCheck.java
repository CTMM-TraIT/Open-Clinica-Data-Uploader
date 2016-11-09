package nl.thehyve.ocdu.validators.clinicalDataChecks;

import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OcDefinitions.CRFDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.ItemDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.OcDefinitions.ProtocolFieldRequirementSetting;
import nl.thehyve.ocdu.models.errors.ErrorClassification;
import nl.thehyve.ocdu.models.errors.MissingPersonIDError;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.validators.UtilChecks;
import org.apache.commons.lang3.StringUtils;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Checks if the person ID is present in the subject data if this field is defined as required in the study. Only active
 * for new subjects, existing subjects can identified using only the <code>StudySubjectID (ssid)</code>.
 * Created by jacob on 8/3/16.
 */
public class MissingPersonIDCrossCheck implements ClinicalDataCrossCheck {
    @Override
    public ValidationErrorMessage getCorrespondingError(List<ClinicalData> data, MetaData metaData, Map<ClinicalData, ItemDefinition> itemDefMap, List<StudySubjectWithEventsType> studySubjectWithEventsTypeList, Map<ClinicalData, Boolean> shownMap, Map<String, Set<CRFDefinition>> eventMap) {
        MissingPersonIDError error = new MissingPersonIDError();

        List<String> existingStudySubjectIDList =
                studySubjectWithEventsTypeList.stream()
                        .map(StudySubjectWithEventsType::getLabel)
                        .collect(Collectors.toList());

        if (metaData.getPersonIDUsage() == ProtocolFieldRequirementSetting.MANDATORY) {
            List<ClinicalData> newSubjectInStudy = data.stream()
                                                    .filter(clinicalData -> ! existingStudySubjectIDList.contains(clinicalData.getSsid()))
                                                    .collect(Collectors.toList());
            Set<String> violatorSet = newSubjectInStudy.stream()
                                        .filter(clinicalData -> StringUtils.isEmpty(clinicalData.getPersonID()))
                                        .map(ClinicalData::getSsid)
                                        .collect(Collectors.toSet());

            UtilChecks.addErrorClassificationForSubjects(data, violatorSet, ErrorClassification.BLOCK_ENTIRE_CRF);
            error.addAllOffendingValues(violatorSet);
        }
        if (error.getOffendingValues().size() > 0)
            return error;
        else return null;
    }
}
