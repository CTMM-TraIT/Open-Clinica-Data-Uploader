package nl.thehyve.ocdu.validators.patientDataChecks;

import nl.thehyve.ocdu.models.OCEntities.Subject;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.OcDefinitions.SiteDefinition;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import org.apache.commons.lang3.StringUtils;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bo on 6/16/16.
 */
public class SitePatientDataCheck implements PatientDataCheck {

    @Override
    public ValidationErrorMessage getCorrespondingError(int index, Subject subject, MetaData metaData,
                                                        List<StudySubjectWithEventsType> subjectWithEventsTypes,
                                                        List<String> ssidsInData) {

        String ssid = subject.getSsid();
        String commonMessage = getCommonErrorMessage(index, ssid);

        ValidationErrorMessage error = null;
        String study = subject.getStudy();
        String site = subject.getSite();

        if (!StringUtils.isBlank(site) && !StringUtils.isBlank(study)) {
            List<SiteDefinition> sites = metaData.getSiteDefinitions();
            if ((sites == null) || (sites.isEmpty())) {
                error = new ValidationErrorMessage("Study does not have sites and site(s) are specified for subjects");
            } else {
                List<String> sitenames = new ArrayList<>();
                for (SiteDefinition sd : sites) {
                    sitenames.add(sd.getUniqueID());
                }
                if (!sitenames.contains(subject.getSite())) {
                    error = new ValidationErrorMessage("One or more study site do not exist, use the site's Unique Protocol ID.");
                }
            }
        }

        if(error != null) {
            error.addOffendingValue(commonMessage + " site: " + subject.getSite());
        }

        return error;
    }

}
