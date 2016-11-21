package nl.thehyve.ocdu.validators;

import nl.thehyve.ocdu.models.NotificationsCollector;
import nl.thehyve.ocdu.models.OCEntities.*;
import nl.thehyve.ocdu.models.OcDefinitions.EventDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.errors.ErrorClassification;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Is responsible to filter data <pre>List<ClinicalData></pre> to remove or to adjust records with errors
 * Created by jacob on 11/8/16.
 */
public class ErrorFilter {
//    private static final Logger log = LoggerFactory.getLogger(ErrorFilter.class);
    private List<ClinicalData> clinicalDataList;
    private List<Event> eventList;
    private List<Subject> subjectList;
    private List<StudySubjectWithEventsType> studySubjectWithEventsTypeList;
    private NotificationsCollector notificationsCollator;
    private Study study;
    private MetaData metaData;


    public ErrorFilter(Study study,
                       MetaData metaData,
                       List<ClinicalData> clinicalDataList,
                       List<Event> eventList,
                       List<Subject> subjectList,
                       List<StudySubjectWithEventsType> studySubjectWithEventsTypeList,
                       NotificationsCollector notificationsCollator) {
        this.study = study;
        this.metaData = metaData;
        this.eventList = eventList;
        this.subjectList = subjectList;
        this.clinicalDataList = clinicalDataList;
        this.studySubjectWithEventsTypeList = studySubjectWithEventsTypeList;
        this.notificationsCollator = notificationsCollator;
    }

    private Tree<OcEntity> createTree() {
        Tree treeMap = new Tree<>(study);
        for (Subject subject : subjectList) {
            treeMap.addLeaf(subject);
            eventList.stream().forEach( event -> {
                if (event.getSsid().equals(subject.getSsid())) {
                    treeMap.addLeaf(subject, event);
                    String eventKey = event.createEventKey();
                    clinicalDataList.stream().forEach( clinicalData ->  {
                        String clinicalDataEventKey = clinicalData.createEventKey();
                        if (eventKey.equals(clinicalDataEventKey)) {
                            treeMap.addLeaf(event, clinicalData);
                        }
                    });
                }
            });
        }
        return treeMap;
    }

    /**
     * Removes data ({@link ClinicalData}, {@link Event} and {@link }Subject}) which contain errors. Also removes any
     * entities associated with the error. E.g. if an subject is not present in OC then event is also removed in case of
     * an error.
     */
    public void filterDataWithErrors() {
        if (UtilChecks.listContainsErrorOfType(clinicalDataList, ErrorClassification.BLOCK_ENTIRE_UPLOAD) ||
            UtilChecks.listContainsErrorOfType(eventList, ErrorClassification.BLOCK_ENTIRE_UPLOAD) ||
            UtilChecks.listContainsErrorOfType(subjectList, ErrorClassification.BLOCK_ENTIRE_UPLOAD)) {
            eventList.clear();
            subjectList.clear();
            clinicalDataList.clear();
            notificationsCollator.addNotification("Error is present which blocks the entire upload. " +
                    "No data was uploaded to " + notificationsCollator.getTargetSystem());
            return;
        }

        Tree<OcEntity> tree = createTree();
        //log.debug("Tree pre filtering  ->\n" + tree);


        List<OcEntity> subjectListWithErrors =
            returnListOfEntitiesWithError(subjectList, ErrorClassification.BLOCK_SUBJECT);
        if ( ! subjectListWithErrors.isEmpty()) {
            Set<String> subjectIDsWithErrorSet =
                    subjectListWithErrors.stream().map(subject -> subject.getSsid()).collect(Collectors.toSet());
            removeAllDataForSubjectID(subjectIDsWithErrorSet);
        }


        List<OcEntity> eventWithErrorList =
                returnListOfEntitiesWithError(eventList, ErrorClassification.BLOCK_EVENT);

        if ( ! eventWithErrorList.isEmpty()) {
            for (OcEntity eventWithError : eventWithErrorList) {
                // When an repeating-event contains an error, we must remove all the subject's events and
                // associated data. The reason is thatOpenClinica does not provide a field to communicate the
                // event occurrence in the schedule event Web-service. The event-occurrence is considered to be
                // the sequential number of the SOAP-message. If we remove only a single event in case of an
                // error, then unintended gaps will be created in the event sequences.
                String eventName = ((Event) eventWithError).getEventName();
                EventDefinition eventDefinition =
                        metaData.findEventDefinitionByName(eventName);
                if (eventDefinition == null) {
                    notificationsCollator.addNotification("Unable to locate definition of event called '" + eventName + "'. Event was removed");
                }
                Subject subject = (Subject) tree.getTree(eventWithError).getParent().getHead();

                removeEventAndAssociatedClinicalData(tree, subject);

            }
        }

        clinicalDataList.removeIf(clinicalData -> clinicalData.hasErrorOfType(ErrorClassification.BLOCK_SINGLE_ITEM));

        // Tree postFilterTree = createTree();
        //log.debug("Tree post filtering ->\n" + postFilterTree);
    }

    private void removeEventAndAssociatedClinicalData(Tree tree, Subject subject) {
        Tree subjectTree = tree.getTree(subject);
        Collection<Tree> eventTreeList = subjectTree.getSubTrees();
        for (Tree eventTree : eventTreeList) {
            Collection<Tree> clinicalDataTree = eventTree.getSubTrees();
            clinicalDataTree.stream().forEach(clincalDataItem -> {
                        ClinicalData clinicalDataToRemove = (ClinicalData) clincalDataItem.getHead();
                        clinicalDataList.removeIf(clinicalData -> clinicalData.getSsid().equals(clinicalDataToRemove.getSsid()));
                    }
            );
            eventList.removeIf(event -> event.equals(eventTree.getHead()));
        }
    }

    private void removeAllDataForSubjectID(Set<String> subjectIDWithErrorSet) {
        UtilChecks.removeFromListIf(subjectList, subject -> subjectIDWithErrorSet.contains(subject.getSsid()));
        UtilChecks.removeFromListIf(eventList, event -> subjectIDWithErrorSet.contains(event.getSsid()));
        UtilChecks.removeFromListIf(clinicalDataList, clinicalData -> subjectIDWithErrorSet.contains(clinicalData.getSsid()));
    }

    public List<OcEntity> returnListOfEntitiesWithError(List<? extends OcEntity> entityList, ErrorClassification errorClassification) {
        return entityList.stream().filter( ocEntity -> ocEntity.hasErrorOfType(errorClassification)).collect(Collectors.toList());
    }
}
