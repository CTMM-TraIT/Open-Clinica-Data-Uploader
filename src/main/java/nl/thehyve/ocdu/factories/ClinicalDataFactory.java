package nl.thehyve.ocdu.factories;

import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OcUser;
import nl.thehyve.ocdu.models.UploadSession;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The only way ClinicalData objects should be created outside tests.
 * This factory is responsible for deserializing ClinicalData points from user text file.
 * It does not save anything to the database nor does any validation - FileValidator needs to check text file
 * before ClinicalDataFactory can process it.
 *
 * Created by piotrzakrzewski on 16/04/16.
 */
public class ClinicalDataFactory extends UserSubmittedDataFactory {


    public final static String STUDY_SUBJECT_ID = "StudySubjectID";
    public final static String PERSON_ID = "PersonID";
    public final static String STUDY = "Study";
    public final static String EventName = "EventName";
    public final static String EventRepeat = "EventRepeat";
    public final static String CRFName = "CRFName";
    public final static String CRFVersion = "CRFVersion";
    public final static String[] MANDATORY_HEADERS = {STUDY_SUBJECT_ID, STUDY, EventName, EventRepeat,
            CRFName, CRFVersion};

    public final static String SITE = "Site";

    public ClinicalDataFactory(OcUser user, UploadSession submission) {
        super(user, submission);
    }

    public List<ClinicalData> createClinicalData(Path dataFile) {
        Optional<String[]> headerRow = getHeaderRow(dataFile);
        Map<String, Integer> columnsIndex = createColumnsIndexMap(headerRow.get());
        try (Stream<String> lines = Files.lines(dataFile)) {
            HashMap<String, Integer> coreColumns = getCoreHeader(columnsIndex);
            List<ClinicalData> clinicalData = new ArrayList<>();
            List<List<ClinicalData>> clinicalDataAggregates = lines.skip(1). // skip header
                    filter(s -> parseLine(s).length > 2). // smallest legal file consists of no less than 3 columns
                    map(s -> parseLine(0, s, columnsIndex, coreColumns)).collect(Collectors.toList());
            AtomicLong counter = new AtomicLong(2);
            clinicalDataAggregates.forEach(aggregate -> {
                aggregate.forEach( clinicalData1 -> clinicalData1.setLineNumber(counter.get()));
                clinicalData.addAll(aggregate);
                counter.incrementAndGet();
            });
            return clinicalData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private HashMap<String, Integer> getCoreHeader(Map<String, Integer> headerMap) {
        HashMap<String, Integer> coreMap = new HashMap<>();
        Integer ssidIndex = getAndRemove(headerMap, STUDY_SUBJECT_ID);
        coreMap.put(STUDY_SUBJECT_ID, ssidIndex);
        Integer personIDIndex = getAndRemove(headerMap, PERSON_ID);
        coreMap.put(PERSON_ID, personIDIndex);
        Integer studyIndex = getAndRemove(headerMap, STUDY);
        coreMap.put(STUDY, studyIndex);
        Integer eventNameIndex = getAndRemove(headerMap, EventName);
        coreMap.put(EventName, eventNameIndex);
        Integer eventRepeatIndex = getAndRemove(headerMap, EventRepeat);
        coreMap.put(EventRepeat, eventRepeatIndex);
        Integer crfNameIndex = getAndRemove(headerMap, CRFName);
        coreMap.put(CRFName, crfNameIndex);
        Integer crfVersionIndex = getAndRemove(headerMap, CRFVersion);
        coreMap.put(CRFVersion, crfVersionIndex);
        Integer siteIndex = getAndRemove(headerMap, SITE);
        coreMap.put(SITE, siteIndex);
        return coreMap;
    }

    private String getOptionalFromArray(String[] array, int index) {
        if (array.length <= index) {
            return "";
        } else return array[index];
    }

    private List<ClinicalData> parseLine(long lineNumber, String line, Map<String,
            Integer> headerMap, HashMap<String, Integer> coreColumns) {
        String[] split = parseLine(line);

        String ssid = split[coreColumns.get(STUDY_SUBJECT_ID)];
        String personID = "";
        // the null-check is needed because the personID is only a required column if the study has configured it to be
        // required.
        if (coreColumns.get(PERSON_ID) != null) {
            personID = split[coreColumns.get(PERSON_ID)];
        }
        String study = split[coreColumns.get(STUDY)];
        String eventName = split[coreColumns.get(EventName)];
        String eventRepeat = split[coreColumns.get(EventRepeat)];
        String crf = getOptionalFromArray(split, coreColumns.get(CRFName));
        String crfVer = getOptionalFromArray(split, coreColumns.get(CRFVersion));
        Integer siteInd = coreColumns.get(SITE);
        String site = null;
        if (siteInd != null) {
            site = getOptionalFromArray(split, siteInd);
        }

        List<ClinicalData> aggregation = new ArrayList<>();
        for (String colName : headerMap.keySet()) {
            String item = parseItem(colName);
            Integer groupRepeat = parseGroupRepeat(colName);
            if (groupRepeat == null) item = colName; // Consequences of encoding group repeat in the column name

            String value = getOptionalFromArray(split, headerMap.get(colName));
            ClinicalData dat = new ClinicalData(lineNumber, study, item, ssid, personID, eventName, eventRepeat, crf,
                    getSubmission(), crfVer, groupRepeat, getUser(), value.trim()); // Mind the trim() on value.
            dat.setSite(site);
            if (StringUtils.isNotEmpty(value)) // ignore empty
                aggregation.add(dat);
        }
        return aggregation;
    }

    private String parseItem(String columnToken) {
        int last = columnToken.lastIndexOf("_");
        if (last > 0) {
            return columnToken.substring(0, last);
        } else {
            return columnToken;
        }
    }

    private Integer parseGroupRepeat(String columnToken) {
        String[] splt = columnToken.split("_");
        Integer gRep = null;
        if (splt.length > 1) {
            try {
                int lastPart = splt.length - 1;
                gRep = Integer.parseInt(splt[lastPart]);
            } catch (NumberFormatException e) {
                gRep = null; // Do nothing
            }
        }
        return gRep;
    }

    private Integer getAndRemove(Map<String, Integer> map, String key) {
        Integer index = map.get(key);
        map.remove(key);
        return index;
    }

}
