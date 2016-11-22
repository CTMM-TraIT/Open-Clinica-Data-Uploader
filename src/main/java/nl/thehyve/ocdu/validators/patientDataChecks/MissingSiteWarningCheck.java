package nl.thehyve.ocdu.validators.patientDataChecks;

import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OCEntities.Subject;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.OcDefinitions.SiteDefinition;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import org.apache.commons.lang3.StringUtils;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.List;
import java.util.Set;

/**
 * Check to warn users of the fact that subjects are going to be uploaded to study-level if the site is missing in the
 * data.
 * Created by jacob on 9/8/16.
 */
public class MissingSiteWarningCheck implements PatientDataCheck {

    @Override
    public ValidationErrorMessage getCorrespondingError(int index, Subject subject, MetaData metaData,
                                                        List<StudySubjectWithEventsType> subjectWithEventsTypes,
                                                        Set<String> ssidsInData, List<String> subjectIDInSubjectInput) {
        String ssid = subject.getSsid();
        String commonMessage = getCommonErrorMessage(index, ssid);

        ValidationErrorMessage error = null;
        String study = subject.getStudy();
        String site = subject.getSite();

        if (StringUtils.isBlank(site) && !StringUtils.isBlank(study)) {
            List<SiteDefinition> sites = metaData.getSiteDefinitions();
            if ((sites != null) && (! sites.isEmpty())) {
                error = new ValidationErrorMessage("No site given for some subjects. If you continue these subjects will be created on study level.");
            }
        }
        if(error != null) {
            error.addOffendingValue(commonMessage);
        }
        return error;
    }
}