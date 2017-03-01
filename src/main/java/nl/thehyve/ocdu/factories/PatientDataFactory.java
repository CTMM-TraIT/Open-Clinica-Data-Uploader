package nl.thehyve.ocdu.factories;

import nl.thehyve.ocdu.models.OCEntities.Subject;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.OcDefinitions.ProtocolFieldRequirementSetting;
import nl.thehyve.ocdu.models.OcUser;
import nl.thehyve.ocdu.models.UploadSession;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The only way Patient objects should be created outside tests.
 * This factory is responsible for deserializing Patient objects from user text file.
 * It does not save anything to the database nor does any validation - FileValidator needs to check text file
 * before PatientDataFactory can process it.
 * Created by bo on 6/6/16.
 */
public class PatientDataFactory extends UserSubmittedDataFactory {

    private boolean onlyYearOfBirthUsed;

    public final static String STUDY_SUBJECT_ID = "Study Subject ID";
    public final static String GENDER = "Gender";
    public final static String DATE_OF_BIRTH = "Date of Birth";
    public final static String YEAR_OF_BIRTH = "Year of Birth";
    public final static String DATE_OF_ENROLLMENT = "Date of Enrollment";
    public final static String PERSON_ID = "Person ID";
    public final static String SECONDARY_ID = "Secondary ID";
    public final static String STUDY = "Unique Protocol ID";
    public final static String SITE = "Site (optional)";
    public final static String[] MANDATORY_HEADERS = {STUDY_SUBJECT_ID, STUDY, SITE};
    public static final String[] ONLY_YEAR_OF_BIRTH_ALL_PERMITTED_COLUMNS = {STUDY_SUBJECT_ID, STUDY, SITE, GENDER,
            YEAR_OF_BIRTH, DATE_OF_ENROLLMENT, PERSON_ID, SECONDARY_ID};

    public static final String[] COMPLETE_BIRTH_DATE_ALL_PERMITTED_COLUMNS = {STUDY_SUBJECT_ID, STUDY, SITE, GENDER,
            DATE_OF_BIRTH, DATE_OF_ENROLLMENT, PERSON_ID, SECONDARY_ID};

    public PatientDataFactory(OcUser user, UploadSession submission, boolean onlyYearOfBirthUsed) {
        super(user, submission);
        this.onlyYearOfBirthUsed = onlyYearOfBirthUsed;
    }

    public List<Subject> createPatientData(Path patientFile) {
        Optional<String[]> headerRow = getHeaderRow(patientFile);
        if (headerRow.isPresent()) {
            Map<String, Integer> columnsIndex = createColumnsIndexMap(headerRow.get());

            try (Stream<String> lines = Files.lines(patientFile)) {
                return lines.skip(1)
                        .map(UserSubmittedDataFactory::parseLine)
                        .map(row -> mapRow(row, columnsIndex))
                        .collect(Collectors.toList());

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } else {
            throw new RuntimeException("Patient file is empty.");
        }

    }

    private Subject mapRow(String[] row, Map<String, Integer> columnsIndex) {
        List<String> arr = new ArrayList<>(Arrays.asList(row));
        if (row.length < columnsIndex.keySet().size()) {
            arr.add("");
        }

        Subject subject = new Subject();
        subject.setOwner(getUser());
        subject.setSubmission(getSubmission());
        setValue(arr.toArray(new String[arr.size()]), columnsIndex, STUDY_SUBJECT_ID, subject::setSsid);
        setValue(arr.toArray(new String[arr.size()]), columnsIndex, GENDER, subject::setGender);
        if (onlyYearOfBirthUsed) {
            setValue(arr.toArray(new String[arr.size()]), columnsIndex, YEAR_OF_BIRTH, subject::setDateOfBirth);
        } else {
            setValue(arr.toArray(new String[arr.size()]), columnsIndex, DATE_OF_BIRTH, subject::setDateOfBirth);
        }
        setValue(arr.toArray(new String[arr.size()]), columnsIndex, PERSON_ID, subject::setPersonId);
        setValue(arr.toArray(new String[arr.size()]), columnsIndex, DATE_OF_ENROLLMENT, subject::setDateOfEnrollment);
        setValue(arr.toArray(new String[arr.size()]), columnsIndex, SECONDARY_ID, subject::setSecondaryId);
        setValue(arr.toArray(new String[arr.size()]), columnsIndex, STUDY, subject::setStudy);
        setValue(arr.toArray(new String[arr.size()]), columnsIndex, SITE, subject::setSite);
        subject.addLeadingZerosInDates();
        return subject;
    }

    private void setValue(String[] row, Map<String, Integer> columnsIndex, String columnName,
                          Consumer<String> consumer) {
        if (!columnsIndex.containsKey(columnName)) {
            return;
        }
        String cellValue = row[columnsIndex.get(columnName)];
        consumer.accept(cellValue);
    }

    public List<String> generatePatientRegistrationTemplate(MetaData metadata, Map<String, String> subjectMap, boolean registerSite, Map<String, String> subjectSiteSet) {
        List<String> result = new ArrayList<>();
        String delim = "\t";
        List<String> header = new ArrayList<>();

        header.add(STUDY_SUBJECT_ID);
        if (metadata.isGenderRequired()) header.add(GENDER);
        if (metadata.getBirthdateRequired() != 3) {
            if (metadata.getBirthdateRequired() == 1) {
                header.add(DATE_OF_BIRTH);
            }
            else {
                header.add(YEAR_OF_BIRTH);
            }
        }
        if (metadata.getPersonIDUsage() != ProtocolFieldRequirementSetting.BANNED) header.add(PERSON_ID);
        header.add(DATE_OF_ENROLLMENT);
        header.add(SECONDARY_ID);
        header.add(STUDY);
        if (registerSite) header.add(SITE);
        result.add(String.join(delim, header) + "\n");

        for (String ssid : subjectMap.keySet()) {
            String techId = subjectMap.get(ssid);
            if (techId == null) {
                List<String> line = new ArrayList<>();
                line.add(ssid);//ssid
                if (metadata.isGenderRequired()) line.add("");//gender
                if (metadata.getBirthdateRequired() != 3) line.add("");//date of birth
                if (metadata.getPersonIDUsage() != ProtocolFieldRequirementSetting.BANNED) line.add("");//person ID
                line.add("");//date of enrollment
                line.add("");//secondary id
                line.add(metadata.getProtocolName());//study
                if (registerSite) {
                    String siteValue = subjectSiteSet.get(ssid);
                    if (siteValue == null) {
                        siteValue = "";
                    }
                    line.add(siteValue);//site
                }
                result.add(String.join(delim, line) + "\n");
            }
        }

        return result;
    }
}
