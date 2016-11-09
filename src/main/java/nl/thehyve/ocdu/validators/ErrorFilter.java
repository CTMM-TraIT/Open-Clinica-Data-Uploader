package nl.thehyve.ocdu.validators;

import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.errors.ErrorClassification;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Is responsible to filter data <pre>List<ClinicalData></pre> to remove or to adjust records with errors
 * Created by jacob on 11/8/16.
 */
public class ErrorFilter {

    private List<ClinicalData> clinicalDataList;

    public ErrorFilter(List<ClinicalData> clinicalDataList) {
        this.clinicalDataList = clinicalDataList;
    }

    /**
     * Adds the {@link nl.thehyve.ocdu.models.errors.ErrorClassification} to all subjects present in the input set.
     * @param subjectIDSet the subjectID's to apply the error classification to.
     * @param errorClassification the required error classification
     */
    public void addErrorToSubjects(Set<String> subjectIDSet, ErrorClassification errorClassification) {
        List<ClinicalData> result = clinicalDataList.stream().filter( clinicalData -> subjectIDSet.contains( clinicalData.getSsid())).collect(Collectors.toList());
        result.forEach( clinicalData -> clinicalData.addErrorClassification(errorClassification));
    }

    public void addErrorToSingleSubject(String subjectID, ErrorClassification errorClassification) {
        List<ClinicalData> result = clinicalDataList.stream().filter( clinicalData -> subjectID.equals(clinicalData.getSsid())).collect(Collectors.toList());
        result.forEach( clinicalData -> clinicalData.addErrorClassification(errorClassification));
    }

    public void addErrorToAll(ErrorClassification errorClassification) {
        clinicalDataList.forEach( clinicalData -> clinicalData.addErrorClassification(errorClassification));
    }
}
