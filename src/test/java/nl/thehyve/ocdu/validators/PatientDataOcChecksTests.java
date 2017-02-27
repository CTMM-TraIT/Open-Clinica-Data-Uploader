package nl.thehyve.ocdu.validators;

import nl.thehyve.ocdu.TestUtils;
import nl.thehyve.ocdu.models.OCEntities.Subject;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.OcDefinitions.ProtocolFieldRequirementSetting;
import nl.thehyve.ocdu.models.OcDefinitions.SiteDefinition;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.soap.ResponseHandlers.GetStudyMetadataResponseHandler;
import nl.thehyve.ocdu.validators.patientDataChecks.*;
import org.junit.Before;
import org.junit.Test;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;


/**
 * Unit tests for the {@link PatientDataOcChecks}.
 * Created by bo on 6/16/16.
 */
public class PatientDataOcChecksTests {

    private MetaData metadata;
    private List<StudySubjectWithEventsType> testSubjectWithEventsTypeList;
    private Set<String> presentInData;
    private List<String> subjectIDInSubjectDataList;
    private List<String> personIDInSubjectDataList;


    @Before
    public void setup() {
        try {
            testSubjectWithEventsTypeList = TestUtils.createStudySubjectWithEventList();
            MessageFactory messageFactory = MessageFactory.newInstance();
            File testFile = new File("docs/responseExamples/Sjogren_STUDY1.xml");
            FileInputStream in = new FileInputStream(testFile);

            SOAPMessage mockedResponseGetMetadata = messageFactory.createMessage(null, in);
            metadata = GetStudyMetadataResponseHandler.parseGetStudyMetadataResponse(mockedResponseGetMetadata);
            presentInData = new HashSet<>();
            subjectIDInSubjectDataList = new ArrayList<>();
            personIDInSubjectDataList = new ArrayList<>();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSuccess() {
        List<Subject> subjects = new ArrayList<>();
        PatientDataOcChecks ocChecks = new PatientDataOcChecks(metadata, subjects, testSubjectWithEventsTypeList, presentInData);
        List<ValidationErrorMessage> errors = ocChecks.getErrors();
        assertThat(errors, empty());
    }

    @Test
    public void testGenderFormat() {
        Subject subject = new Subject();
        subject.setSsid("1234");
        subject.setGender("wrongGenderFormat");

        GenderPatientDataCheck check = new GenderPatientDataCheck();
        ValidationErrorMessage error = check.getCorrespondingError(0, subject, metadata, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);

        assertThat(error.getMessage(), containsString("Gender"));


        subject.setGender("");
        error = check.getCorrespondingError(0, subject, metadata, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertThat(error.getMessage(), containsString("Gender needs to be specified as m or f"));

        subject.setGender(null);
        error = check.getCorrespondingError(0, subject, metadata, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertThat(error.getMessage(), containsString("Gender needs to be specified as m or f"));
    }

    @Test
    public void testBirthdateFormatYearOnly() {
        Subject subject = new Subject();
        subject.setSsid("1234");

        //invalid format
        subject.setDateOfBirth("198x");
        DateOfBirthPatientDataCheck check = new DateOfBirthPatientDataCheck();
        ValidationErrorMessage error = check.getCorrespondingError(0, subject, metadata, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertThat(error.getMessage(), containsString("invalid"));
    }

    @Test
    public void testGetErrors() {
        List<Subject> subjectList = new ArrayList<>();
        Subject subject = new Subject();
        subject.setSsid("EV-XXXXX");
        subject.setGender("m");
        subject.setPersonId("Wizzard");
        subject.setStudy("Sjogren");
        subject.setDateOfEnrollment("01-01-2010");
        subject.setDateOfBirth("1932");
        subjectList.add(subject);

        Set<String> duplicatePresentInDataList = new HashSet<>();
        duplicatePresentInDataList.add("EV-XXXXX");


        PatientDataOcChecks patientDataOcChecks = new PatientDataOcChecks(metadata, subjectList, testSubjectWithEventsTypeList, duplicatePresentInDataList);

        List<ValidationErrorMessage> errorList = patientDataOcChecks.getErrors();
        assertEquals(0, errorList.size());
    }

    @Test
    public void testDuplicateSubjectLabelInData() {
        List<Subject> subjectList = new ArrayList<>();
        Subject subject = new Subject();
        subject.setSsid("EV-YYYYY");
        subject.setGender("m");
        subject.setPersonId("Wizzard");
        subject.setStudy("Sjogren");
        subject.setDateOfBirth("2000");
        subject.setDateOfEnrollment("01-01-2010");
        subjectList.add(subject);

        subject = new Subject();
        subject.setSsid("EV-YYYYY");
        subject.setGender("m");
        subject.setStudy("Sjogren");
        subject.setDateOfBirth("2000");
        subject.setDateOfEnrollment("01-01-2010");
        // Npte: different PersonID
        subject.setPersonId("pupil Wizzard");
        subjectList.add(subject);

        Set<String> presentInDataList = new HashSet<>();
        presentInDataList.add("EV-YYYYY");
        PatientDataOcChecks patientDataOcChecks = new PatientDataOcChecks(metadata, subjectList, testSubjectWithEventsTypeList, presentInDataList);

        List<ValidationErrorMessage> errorList = patientDataOcChecks.getErrors();
        assertEquals(1, errorList.size());
        assertThat(errorList.get(0).getMessage(), containsString("Duplicate subject ID found in data"));
        Collection<String> offendingValues = errorList.get(0).getOffendingValues();

        assertEquals(true, offendingValues.contains("Line 1 (subjectID = EV-YYYYY) : "));
    }


    @Test
    public void testDuplicatePersonIDInData() {
        List<Subject> subjectList = new ArrayList<>();
        Subject subject = new Subject();
        subject.setSsid("EV-YYYYY");
        subject.setGender("m");
        subject.setPersonId("Wizzard");
        subject.setStudy("Sjogren");
        subject.setDateOfBirth("2000");
        subject.setDateOfEnrollment("01-01-2010");
        subjectList.add(subject);

        subject = new Subject();
        subject.setSsid("EV-ZZZZZ");
        subject.setGender("m");
        subject.setStudy("Sjogren");
        subject.setDateOfBirth("2000");
        subject.setDateOfEnrollment("01-01-2010");
        // Npte: different PersonID
        subject.setPersonId("Wizzard");
        subjectList.add(subject);

        Set<String> presentInDataList = new HashSet<>();
        presentInDataList.add("EV-YYYYY");
        presentInDataList.add("EV-ZZZZZ");
        PatientDataOcChecks patientDataOcChecks = new PatientDataOcChecks(metadata, subjectList, testSubjectWithEventsTypeList, presentInDataList);

        List<ValidationErrorMessage> errorList = patientDataOcChecks.getErrors();
        assertEquals(1, errorList.size());
        assertThat(errorList.get(0).getMessage(), containsString("Duplicate person ID present in subject data"));
        Collection<String> offendingValues = errorList.get(0).getOffendingValues();

        assertEquals(true, offendingValues.contains("Line 1 (subjectID = EV-YYYYY) :  person ID: Wizzard"));
    }

    @Test
    public void testDuplicateSubjectID() {
        List<Subject> subjectList = new ArrayList<>();
        Subject subject = new Subject();
        subject.setSsid("EV-XXXXX");
        subject.setGender("m");
        subject.setPersonId("Wizzard");
        subject.setStudy("Sjogren");
        subject.setDateOfBirth("2000");
        subject.setDateOfEnrollment("01-01-2010");
        subjectList.add(subject);

        HashSet<String> presentInDataList = new HashSet<>();
        presentInDataList.add("EV-YYYYY");

        PatientDataOcChecks patientDataOcChecks = new PatientDataOcChecks(metadata, subjectList, testSubjectWithEventsTypeList, presentInDataList);

        List<ValidationErrorMessage> errorList = patientDataOcChecks.getErrors();
        assertEquals(1, errorList.size());
        assertThat(errorList.get(0).getMessage(), containsString("One or more subjects are absent in the data file"));
    }

    @Test
    public void testAutoGeneratedSubjectIDPatientCheck() throws Exception {
        List<Subject> subjectList = new ArrayList<>();
        Subject subject = new Subject();
        subject.setSsid("EV-XXXXX");
        subject.setGender("m");
        subject.setPersonId("Wizzard");
        subject.setStudy("AutoGeneratedSubjectID");
        subject.setDateOfBirth("01-02-2000");
        subject.setDateOfEnrollment("01-01-2010");
        subjectList.add(subject);

        HashSet<String> presentInDataList = new HashSet<>();
        presentInDataList.add("EV-XXXXX");

        MetaData metaDataAutoGenerated = TestUtils.parseMetaData("docs/responseExamples/metadataAutoGenerated.xml");

        PatientDataOcChecks patientDataOcChecks = new PatientDataOcChecks(metaDataAutoGenerated, subjectList, testSubjectWithEventsTypeList, presentInDataList);

        List<ValidationErrorMessage> errorList = patientDataOcChecks.getErrors();
        assertEquals(1, errorList.size());
        assertThat(errorList.get(0).getMessage(), containsString("Study is configured to use auto-generated subject-ID's, subjects will not be created from subjects file."));
    }

    @Test
    public void testBirthDateFormatFullDate() {
        Subject subject = new Subject();
        subject.setSsid("1234");
        int savedBirthDateRequired = metadata.getBirthdateRequired();
        metadata.setBirthdateRequired(MetaData.BIRTH_DATE_AS_FULL_DATE);
        //future birthday
        subject.setDateOfBirth("01-06-3012");
        subject.setSsid("John Doe");
        DateOfBirthPatientDataCheck check = new DateOfBirthPatientDataCheck();
        ValidationErrorMessage error = check.getCorrespondingError(0, subject, metadata, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertThat(error.getMessage(), containsString("past"));

        subject.setDateOfBirth("01-06-2012");
        check = new DateOfBirthPatientDataCheck();
        error = check.getCorrespondingError(0, subject, metadata, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertNull(error);

        // both day of month and month are missing leading zeros
        subject.setDateOfBirth("1-6-2012");
        check = new DateOfBirthPatientDataCheck();
        error = check.getCorrespondingError(0, subject, metadata, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertThat(error.getMessage(), containsString("Birth date format is invalid"));

        // Month is missing a leading zero
        subject.setDateOfBirth("01-3-2010");
        check = new DateOfBirthPatientDataCheck();
        error = check.getCorrespondingError(0, subject, metadata, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertThat(error.getMessage(), containsString("Birth date format is invalid"));

        // Day of month is missing a leading zero
        subject.setDateOfBirth("1-03-2010");
        check = new DateOfBirthPatientDataCheck();
        error = check.getCorrespondingError(0, subject, metadata, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertThat(error.getMessage(), containsString("Birth date format is invalid"));

        // Year of birth missing 19
        subject.setDateOfBirth("01-03-68");
        check = new DateOfBirthPatientDataCheck();
        error = check.getCorrespondingError(0, subject, metadata, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertThat(error.getMessage(), containsString("Birth date format is invalid"));

        subject.setDateOfBirth(null);
        error = check.getCorrespondingError(0, subject, metadata, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertThat(error.getMessage(), containsString("Date of birth is missing"));

        metadata.setBirthdateRequired(MetaData.BIRTH_DATE_AS_ONLY_YEAR);

        subject.setDateOfBirth("1974");
        error = check.getCorrespondingError(0, subject, metadata, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertNull(error);

        subject.setDateOfBirth("XXXX");
        error = check.getCorrespondingError(0, subject, metadata, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertThat(error.getMessage(), containsString("The year should be four digits"));

        subject.setDateOfBirth("4000");
        error = check.getCorrespondingError(0, subject, metadata, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertThat(error.getMessage(), containsString("Birth year can not be greater than current year"));

        metadata.setBirthdateRequired(savedBirthDateRequired);
    }

    @Test
    public void testDateOfEnrollmentEmpty() {
        Subject subject = new Subject();
        subject.setSsid("1234");

        //empty date of enrollment, today's date is used
        subject.setDateOfEnrollment("");
        DateOfEnrollmentPatientDataCheck check = new DateOfEnrollmentPatientDataCheck();
        ValidationErrorMessage error = check.getCorrespondingError(0, subject, metadata, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertThat(error.getMessage(), containsString("Today"));
    }

    @Test
    public void testDateOfEnrollmentFuture() {
        Subject subject = new Subject();
        subject.setSsid("1234");

        //date of enrollment should be in the past
        subject.setDateOfEnrollment("01-06-3012");
        DateOfEnrollmentPatientDataCheck check = new DateOfEnrollmentPatientDataCheck();
        ValidationErrorMessage error = check.getCorrespondingError(0, subject, metadata, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertThat(error.getMessage(), containsString("past"));
    }

    @Test
    public void testDateOfEnrollmentInvalidFormat() {
        Subject subject = new Subject();
        subject.setSsid("1234");

        //invalid date format
        subject.setDateOfEnrollment("01-JU");
        DateOfEnrollmentPatientDataCheck check = new DateOfEnrollmentPatientDataCheck();
        ValidationErrorMessage error = check.getCorrespondingError(0, subject, metadata, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertThat(error.getMessage(), containsString("invalid"));
    }

    @Test
    public void testPersonIdProvided() {
        Subject subject = new Subject();
        subject.setSsid("1234");

        //person id is provided
        metadata.setPersonIDUsage(ProtocolFieldRequirementSetting.MANDATORY);
        subject.setPersonId("");
        PersonIdPatientDataCheck check = new PersonIdPatientDataCheck();
        ValidationErrorMessage error = check.getCorrespondingError(0, subject, metadata, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertThat(error.getMessage(), containsString("Person"));

        metadata.setPersonIDUsage(ProtocolFieldRequirementSetting.OPTIONAL);
        error = check.getCorrespondingError(0, subject, metadata, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertEquals(error, null);

        metadata.setPersonIDUsage(ProtocolFieldRequirementSetting.BANNED);
        error = check.getCorrespondingError(0, subject, metadata, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertEquals(error, null);

        metadata.setPersonIDUsage(ProtocolFieldRequirementSetting.MANDATORY);
        subject.setPersonId("1345");
        error = check.getCorrespondingError(0, subject, metadata, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertEquals(error, null);

        metadata.setPersonIDUsage(ProtocolFieldRequirementSetting.OPTIONAL);
        error = check.getCorrespondingError(0, subject, metadata, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertEquals(error, null);

        metadata.setPersonIDUsage(ProtocolFieldRequirementSetting.BANNED);
        error = check.getCorrespondingError(0, subject, metadata, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertEquals(error, null);

    }


    @Test
    public void testSecondaryIdTooLong() {
        Subject subject = new Subject();
        subject.setSsid("1234");

        //secondary id is provided, but too long
        subject.setSecondaryId("1111112222222222333333333444444444445555555555666666666667777777777888888");
        SecondaryIdPatientDataCheck check = new SecondaryIdPatientDataCheck();
        ValidationErrorMessage error = check.getCorrespondingError(0, subject, metadata, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertThat(error.getMessage(), containsString("length"));
    }

    @Test
    public void testStudyNotProvided() {
        Subject subject = new Subject();
        subject.setSsid("1234");

        //study is not provided
        subject.setStudy("");
        StudyPatientDataCheck check = new StudyPatientDataCheck();
        ValidationErrorMessage error = check.getCorrespondingError(0, subject, metadata, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertThat(error.getMessage(), containsString("Study should"));

        subject.setStudy("STUDY_NOT_IN_METADATA");
        error = check.getCorrespondingError(0, subject, metadata, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertThat(error.getMessage(), containsString("Study provided in the template is inconsistent with the study defined in the data file"));
    }

    @Test
    public void testSitesNotExist() {
        Subject subject = new Subject();
        subject.setSsid("1234");

        //sites do not exist
        subject.setStudy("S_STUDY1");
        subject.setSite("myownsitethatdoesnotexist");
        SitePatientDataCheck check = new SitePatientDataCheck();
        ValidationErrorMessage error = check.getCorrespondingError(0, subject, metadata, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertThat(error.getMessage(), containsString("Study does not have sites and site(s) are specified for subjects"));

        MetaData localMetaData = new MetaData();
        localMetaData.setSiteDefinitions(null);
        error = check.getCorrespondingError(0, subject, localMetaData, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertThat(error.getMessage(), containsString("Study does not have sites and site(s) are specified for subjects"));
    }


    @Test
    public void testWarningSubjectMissingSite() {
        Subject subject = new Subject();
        subject.setSsid("1234");
        SiteDefinition siteDefinition = new SiteDefinition();
        siteDefinition.setUniqueID("St. Elsewhere");
        siteDefinition.setGenderRequired(true);
        siteDefinition.setSiteOID("SITE_0123");
        siteDefinition.setName("Saint Elsewhere's");
        List<SiteDefinition> siteDefinitionList = new ArrayList<>();
        siteDefinitionList.add(siteDefinition);
        metadata.setSiteDefinitions(siteDefinitionList);

        //sites do not exist
        subject.setStudy("S_STUDY1");
        subject.setSite("");
        MissingSiteWarningCheck check = new MissingSiteWarningCheck();
        ValidationErrorMessage error = check.getCorrespondingError(0, subject, metadata, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertThat(error.getMessage(), containsString("No site given for some subjects. If you continue these subjects will be created on study level."));
    }

    @Test
    public void testSiteExistSuccess() {
        Subject subject = new Subject();
        subject.setSsid("1234");

        //sites do not exist
        subject.setStudy("S_STUDY1");
        subject.setSite("SjogrenSjogren");
        SitePatientDataCheck check = new SitePatientDataCheck();
        List<SiteDefinition> siteDefs = new ArrayList<>();
        SiteDefinition sjogrenSite = new SiteDefinition();
        sjogrenSite.setUniqueID("SjogrenSjogren");
        siteDefs.add(sjogrenSite);
        metadata.setSiteDefinitions(siteDefs);
        ValidationErrorMessage error = check.getCorrespondingError(0, subject, metadata, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertNull(error);

    }

    @Test
    public void bannedGenderTest() throws Exception {
        MetaData metaData = new MetaData();

        metaData.setGenderRequired(false);
        Subject subjectWithGender = new Subject();
        subjectWithGender.setGender("m");
        subjectWithGender.setSsid("John Doe");
        GenderPatientDataCheck check = new GenderPatientDataCheck();
        int bogusLineNumber = 1;
        ValidationErrorMessage error = check.getCorrespondingError(bogusLineNumber, subjectWithGender, metaData,
                testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertThat(error, is(notNullValue()));
        assertThat(error.getMessage(), containsString("It is not allowed to upload gender by the study protocol"));
    }

    @Test
    public void siteGenderTest() throws Exception {
        MetaData metaData = new MetaData();

        metaData.setGenderRequired(false);
        SiteDefinition siteDefinition = new SiteDefinition();
        siteDefinition.setGenderRequired(true);
        siteDefinition.setSiteOID("SITE_NAME");
        Subject subjectWithGender = new Subject();
        subjectWithGender.setGender("asdfasfd");
        subjectWithGender.setSsid("John Doe");

        subjectWithGender.setSite("SITE_NAME");
        List<SiteDefinition> siteDefinitionList = new ArrayList<>();
        siteDefinitionList.add(siteDefinition);
        metaData.setSiteDefinitions(siteDefinitionList);


        GenderPatientDataCheck check = new GenderPatientDataCheck();
        int bogusLineNumber = 1;
        ValidationErrorMessage error = check.getCorrespondingError(bogusLineNumber, subjectWithGender, metaData,
                testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertThat(error, is(notNullValue()));
        assertThat(error.getMessage(), containsString("Gender needs to be specified as m or f."));

        siteDefinitionList.clear();
        siteDefinition.setGenderRequired(false);
        siteDefinitionList.add(siteDefinition);

        error = check.getCorrespondingError(bogusLineNumber, subjectWithGender, metaData,
                testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertThat(error.getMessage(), containsString("It is not allowed to upload gender by the study protocol"));
    }

    @Test
    public void bannedDobTest() throws Exception {
        MetaData localMetaData = new MetaData();
        localMetaData.setBirthdateRequired(MetaData.BIRTH_DATE_NOT_USED);
        Subject subjectWithDOB = new Subject();
        Subject subjectWithDOBFull = new Subject();
        subjectWithDOBFull.setDateOfBirth("01-JUN-2000");
        subjectWithDOB.setDateOfBirth("1997");
        subjectWithDOB.setSsid("John Doe");
        subjectWithDOBFull.setSsid("John Doe");
        DateOfBirthPatientDataCheck check = new DateOfBirthPatientDataCheck();
        int bogusLineNumber = 1;
        ValidationErrorMessage error = check.getCorrespondingError(bogusLineNumber, subjectWithDOB, localMetaData, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        ValidationErrorMessage errorFullYear = check.getCorrespondingError(bogusLineNumber, subjectWithDOBFull, localMetaData, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertThat(error, is(notNullValue()));
        assertThat(error.getMessage(), containsString("Date of birth submission is not allowed by the study protocol"));
        assertThat(errorFullYear.getMessage(), is(notNullValue()));
        assertThat(errorFullYear.getMessage(), containsString("Date of birth submission is not allowed by the study protocol"));
    }

    @Test
    public void subjectAlreadyRegisteredTest() throws Exception {
        Subject subject = new Subject();
        subject.setSsid("EV-00002");

        SubjectNotRegistered check = new SubjectNotRegistered();
        ValidationErrorMessage error = check.getCorrespondingError(0, subject, metadata, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);

        assertThat(error.getMessage(), containsString("already registered"));
    }

    @Test
    public void subjectPresentInTheData() throws Exception {
        Subject subject = new Subject();
        String s1 = "EV-00002";
        subject.setSsid(s1);

        PresentInData check = new PresentInData();
        ValidationErrorMessage error = check.getCorrespondingError(0, subject, metadata, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);

        assertThat(error.getMessage(), containsString("One or more subjects are absent in the data file"));
        presentInData.add(s1);
        error = check.getCorrespondingError(0, subject, metadata, testSubjectWithEventsTypeList, presentInData,subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertThat("Returns null for subject present in the data-file", error, nullValue());
    }

    @Test
    public void duplicatedPersonID() throws  Exception {
        Subject subject = new Subject();
        String s1 = "EV-XXXXXX";
        subject.setSsid(s1);
        subject.setPersonId("EV-00002");
        metadata.setPersonIDUsage(ProtocolFieldRequirementSetting.MANDATORY);
        DuplicatePersonIdDataCheck check = new DuplicatePersonIdDataCheck();
        ValidationErrorMessage error = check.getCorrespondingError(0, subject, metadata, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertThat(error.getMessage(), containsString("Person ID present in subject data is already used in OpenClinica"));

        metadata.setPersonIDUsage(ProtocolFieldRequirementSetting.OPTIONAL);
        error = check.getCorrespondingError(0, subject, metadata, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertEquals(error, null);

        metadata.setPersonIDUsage(ProtocolFieldRequirementSetting.BANNED);
        error = check.getCorrespondingError(0, subject, metadata, testSubjectWithEventsTypeList, presentInData, subjectIDInSubjectDataList, personIDInSubjectDataList);
        assertEquals(error, null);
    }
}
