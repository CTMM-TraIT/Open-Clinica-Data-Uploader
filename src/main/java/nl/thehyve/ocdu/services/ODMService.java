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

package nl.thehyve.ocdu.services;

import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OcDefinitions.*;
import nl.thehyve.ocdu.models.UploadSession;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Converts upload data to a ODM string which can be used in SOAP-envelopes or uploaded manually to OpenClinica.
 * Created by jacob on 6/8/16.
 */
@Service
public class ODMService {

    private static final String STUDY_SUBJECT_PARAM = "${SUBJECT_OID}";

    private static final String STUDY_EVENT_DATA_PARAM = "${STUDY_EVENT_DATA}";

    private static final String STUDY_EVENT_REPEAT_ORDINAL_PARAM = "${STUDY_EVENT_REPEAT_ORDINAL}";

    private static final String CRF_OID_PARAM = "${CRF_OID}";

    private static final String ITEM_GROUP_OID_PARAM = "${ITEM_GROUP_OID_PARAM}";

    private static final String ITEM_DATA_PARAM = "${ITEM_DATA_PARAM}";

    private static final String ITEM_OID_PARAM = "${ITEM_OID_PARAM}";

    private static final String ITEM_VALUE_PARAM = "${ITEM_VALUE_PARAM}";

    private static final String ITEM_GROUP_REPEAT_KEY_PARAM = "${ITEM_GROUP_REPEAT_KEY_PARAM}";

    private static final String STATUS_AFTER_UPLOAD_PARAM = "${STATUS_AFTER_UPLOAD_PARAM}";

    private static final String ITEM_GROUP_SECTION_ANCHOR = "${ITEM_GROUP_SECTION_ANCHOR}";

    /**
     * Template for the subjects section in an ODM-file
     */
    private static final String ODM_SUBJECT_SECTION =
            "<SubjectData SubjectKey=\"" + STUDY_SUBJECT_PARAM + "\">"
          + "<StudyEventData StudyEventOID=\""+ STUDY_EVENT_DATA_PARAM + "\" StudyEventRepeatKey=\"" + STUDY_EVENT_REPEAT_ORDINAL_PARAM + "\">"
          + "<FormData FormOID=\"" + CRF_OID_PARAM + "\" OpenClinica:Status=\"" + STATUS_AFTER_UPLOAD_PARAM + "\">"
          + ITEM_GROUP_SECTION_ANCHOR
          + "</FormData>"
          + "</StudyEventData>"
          + "</SubjectData>";

    /**
     * Template for the section of the individual items.
     */
    private static final String ODM_ITEM_SECTION =
            "<ItemData ItemOID=\"" + ITEM_OID_PARAM + "\" Value=\"" + ITEM_VALUE_PARAM + "\"/>";

    private static final String ITEM_GROUP_SECTION =
            "<ItemGroupData ItemGroupOID=\"" + ITEM_GROUP_OID_PARAM + "\" ItemGroupRepeatKey=\"" + ITEM_GROUP_REPEAT_KEY_PARAM + "\" TransactionType=\"Insert\" >"
            + ITEM_DATA_PARAM
            + "</ItemGroupData>";

    private static final Logger log = LoggerFactory.getLogger(ODMService.class);


    /**
     * Generates a string in ODM-format for uploading.
     * @param clinicalDataList the clinical data
     * @param metaData the study's metadata
     * @param uploadSession the upload session
     * @param subjectLabelToOIDMap a map which mapps the subject's label to the subject's OID.
     * @return the ODM as string
     * @throws Exception
     */
    public String generateODM(List<ClinicalData> clinicalDataList,
                              MetaData metaData,
                              UploadSession uploadSession,
                              Map<String, String> subjectLabelToOIDMap) throws Exception {
        StringBuffer odmDocument =
                buildODM(clinicalDataList, uploadSession, metaData, subjectLabelToOIDMap);
        return odmDocument.toString();
    }


    private void addODMDocumentHeader(String studyOID, StringBuffer odmData,  UploadSession uploadSession) throws Exception {
        odmData.append("<ODM ");
        odmData.append("ODMVersion=\"1.3\" ");
        odmData.append("FileOID=\"");
        odmData.append(System.currentTimeMillis());
        odmData.append("\" ");
        odmData.append("FileType=\"Snapshot\" ");
        odmData.append("Description=\"Dataset ODM\" ");
        odmData.append("CreationDateTime=\"");

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
        String dateTimeStamp = df.format(GregorianCalendar.getInstance().getTime());
        odmData.append(dateTimeStamp);
        odmData.append("\">");
        odmData.append("<ClinicalData StudyOID=\"");
        odmData.append(studyOID);
        odmData.append("\" ");
        odmData.append("MetaDataVersionOID=\"v1.0.0\">");
        odmData.append("<UpsertOn NotStarted=\"");
        odmData.append(uploadSession.isUponNotStarted());
        odmData.append("\" DataEntryStarted=\"");
        odmData.append(uploadSession.isUponDataEntryStarted());
        odmData.append("\" DataEntryComplete=\"");
        odmData.append(uploadSession.isUponDataEntryCompleted());
        odmData.append("\"/>");
    }

    private void addClosingTags(StringBuffer odmData) {
        odmData.append("</ClinicalData>");
        odmData.append("</ODM>");
    }

    private void appendSubjectODMSection(StringBuffer odmData,
                                         MetaData metaData,
                                         List<ClinicalData> clinicalDataList,
                                         String statusAfterUpload,
                                         Map<String, String> eventNameOIDMap,
                                         Map<String, String> itemNameOIDMap,
                                         Map<String, String> subjectLabelToOIDMap) {
        // should not be possible but we just to be sure
        if (clinicalDataList.size() == 0) {
            return;
        }
        String subjectLabel = clinicalDataList.get(0).getSsid();
        String subjectOID = subjectLabelToOIDMap.get(subjectLabel);
        if (StringUtils.isEmpty(subjectOID)) {
            throw new IllegalStateException("No subjectOID present in OpenClinica for subject with label: '" + subjectLabel + "'");
        }
        String eventName = eventNameOIDMap.get(clinicalDataList.get(0).getEventName());
        String eventRepeatOrdinal = clinicalDataList.get(0).getEventRepeat();
        String crfName = clinicalDataList.get(0).getCrfName();
        String crfVersion = clinicalDataList.get(0).getCrfVersion();
        String crfOID = metaData.findFormOID(crfName, crfVersion);
        String itemGroupOID = clinicalDataList.get(0).getItemGroupOID();

        StrBuilder builder = new StrBuilder(ODM_SUBJECT_SECTION);
        builder.replaceAll(STUDY_SUBJECT_PARAM, subjectOID);
        builder.replaceAll(STUDY_EVENT_DATA_PARAM, eventName);
        builder.replaceAll(STUDY_EVENT_REPEAT_ORDINAL_PARAM, eventRepeatOrdinal);
        builder.replaceAll(CRF_OID_PARAM, crfOID);
        builder.replaceAll(STATUS_AFTER_UPLOAD_PARAM, statusAfterUpload);


        Map<Integer, List<ClinicalData>> clinicalDataPerGroupRepeatMap
                = createClinicalDataPerGroupRepeatMap(clinicalDataList);



        StringBuilder itemGroupCollator = new StringBuilder();
        for (Integer itemGroupRepeatNumber : clinicalDataPerGroupRepeatMap.keySet()) {
            StrBuilder itemGroupBuilder = new StrBuilder(ITEM_GROUP_SECTION);
            itemGroupBuilder.replaceAll(ITEM_GROUP_OID_PARAM, itemGroupOID);
            itemGroupBuilder.replaceFirst(ITEM_GROUP_REPEAT_KEY_PARAM, itemGroupRepeatNumber == null ? "1" : itemGroupRepeatNumber.toString());
            List<ClinicalData> itemGroupDataList = clinicalDataPerGroupRepeatMap.get(itemGroupRepeatNumber);
            StrBuilder itemDataBuilder = new StrBuilder();
            for (ClinicalData clinicalData : itemGroupDataList) {
                StrBuilder itemBuilder = new StrBuilder(ODM_ITEM_SECTION);
                itemBuilder.replaceAll(ITEM_OID_PARAM, StringEscapeUtils.escapeXml(itemNameOIDMap.get(clinicalData.getItem())));
                itemBuilder.replaceAll(ITEM_VALUE_PARAM, StringEscapeUtils.escapeXml(clinicalData.getValue()));
                itemDataBuilder.append(itemBuilder);

            }
            itemGroupBuilder.replaceAll(ITEM_DATA_PARAM, itemDataBuilder.toString());
            itemGroupCollator.append(itemGroupBuilder.toString());
        }


        builder.replaceAll(ITEM_GROUP_SECTION_ANCHOR, itemGroupCollator.toString());

        odmData.append(builder.toStringBuffer());
    }


    /**
     * Converts the clinical data list to a map with the item group repeat number as a key and a list of all the items
     * belonging to that repeat number
     * @param clinicalDataList the input clinical data list
     * @return a map with the clinicaldata list per group repeat
     */
    private Map<Integer, List<ClinicalData>> createClinicalDataPerGroupRepeatMap(List<ClinicalData> clinicalDataList) {
        Map<Integer, List<ClinicalData>> ret = new HashMap<>();
        for (ClinicalData clinicalData : clinicalDataList) {
            Integer groupRepeatNumber = clinicalData.getGroupRepeat();
            List<ClinicalData> targetList;
            if (! ret.containsKey(groupRepeatNumber)) {
                targetList = new ArrayList<>(50);
                ret.put(groupRepeatNumber, targetList);
            }
            else {
                targetList = ret.get(groupRepeatNumber);
            }
            targetList.add(clinicalData);
        }
        return ret;
    }

    private void addItemGroupOID(List<ClinicalData> clinicalDataList, MetaData metaData) {
        Map<String, String> itemNameItemGroupOIDMap = new HashMap<>();
        for (ItemGroupDefinition itemGroupDefinition : metaData.getItemGroupDefinitions()) {
            for (ItemDefinition itemDefinition : itemGroupDefinition.getItems()) {
                itemNameItemGroupOIDMap.put(itemDefinition.getName(), itemGroupDefinition.getOid());
            }
        }

        for (ClinicalData clinicalData : clinicalDataList) {
            clinicalData.setItemGroupOID(itemNameItemGroupOIDMap.get(clinicalData.getItem()));
        }
    }

    private StringBuffer buildODM(List<ClinicalData> clinicalDataList,
                                  UploadSession uploadSession,
                                  MetaData metaData,
                                  Map<String, String> subjectLabelToOIDMap) throws Exception {
        long startTime = System.currentTimeMillis();

        StringBuffer odmData = new StringBuffer("");
        if (clinicalDataList.size() == 0) {
            return odmData;
        }

        addItemGroupOID(clinicalDataList, metaData);

        String studyOID = metaData.getStudyOID();

        addODMDocumentHeader(studyOID, odmData, uploadSession);

        Map<String, String> eventNameOIDMap =
                metaData.getEventDefinitions().stream().collect(Collectors.toMap(EventDefinition::getName, EventDefinition::getStudyEventOID));

        String eventName = clinicalDataList.get(0).getEventName();
        String eventOID = eventNameOIDMap.get(eventName);
        String crfName = clinicalDataList.get(0).getCrfName();
        String crfVersion = clinicalDataList.get(0).getCrfVersion();
        EventDefinition eventDefinition =
                metaData.getEventDefinitions().stream().filter(eventDefinition1 -> eventDefinition1.getStudyEventOID().equals(eventOID)).findFirst().get();
        CRFDefinition crfDefinition = eventDefinition.getCrfDefinitions().stream().filter( crfDefinition1 -> (
                (crfDefinition1.getName().equals(crfName)) &&
                (crfDefinition1.getVersion().equals(crfVersion)))).findFirst().get();

        Set<ItemDefinition> allItemDefinitions = new HashSet<>();
        metaData.getItemGroupDefinitions().forEach(itemGroupDefinition -> {
            for (ItemDefinition itemDefinition : itemGroupDefinition.getItems()) {
                List<ItemPresentInForm> itemPresentInFormList = itemDefinition.getItemPresentInFormList();
                for (ItemPresentInForm itemPresentInForm : itemPresentInFormList) {
                    if (crfDefinition.getOid().equals(itemPresentInForm.getFormOID())) {
                        allItemDefinitions.add(itemDefinition);
                    }
                }
            }
        });

        Map<String, String> itemNameOIDMap =
                allItemDefinitions.stream().collect(Collectors.toMap(ItemDefinition::getName, ItemDefinition::getOid));

        Map<String, List<ClinicalData>> outputMap = clinicalDataList.stream().collect(Collectors.groupingBy(ClinicalData::createODMKey,
                Collectors.toList()));

        TreeMap<String, List<ClinicalData>> sortedMap = new TreeMap<>(outputMap);
        for (String key : sortedMap.keySet()) {
            List<ClinicalData> outputClinicalData = sortedMap.get(key);
            appendSubjectODMSection(odmData, metaData, outputClinicalData, uploadSession.getCrfStatusAfterUpload(), eventNameOIDMap, itemNameOIDMap, subjectLabelToOIDMap);
        }

        addClosingTags(odmData);
        long duration = System.currentTimeMillis() - startTime;
        log.info("Finished ODM generation for study " + studyOID + " in " + duration + " milliseconds for " + clinicalDataList.size() + " data points");

        return odmData;
    }
}
