package nl.thehyve.ocdu.validators;

import nl.thehyve.ocdu.controllers.UploadSessionController;
import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OCEntities.Event;
import nl.thehyve.ocdu.models.OCEntities.Subject;
import nl.thehyve.ocdu.models.errors.ErrorClassification;
import nl.thehyve.ocdu.services.InputValidationException;
import org.junit.Assert;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit test for {@link UtilChecks}
 * Created by jacob on 8/9/16.
 */
public class UtilChecksTests {

    @Test
    public void testIsInteger() throws Exception {
        Assert.assertEquals(false, UtilChecks.isInteger("ABCE"));

        Assert.assertEquals(false, UtilChecks.isInteger("3.14"));
        Assert.assertEquals(false, UtilChecks.isInteger("3,14"));
        Assert.assertEquals(false, UtilChecks.isInteger("3,.14"));

        Assert.assertEquals(false, UtilChecks.isInteger("-3654-"));

        Assert.assertEquals(true, UtilChecks.isInteger("1234"));
    }

    @Test
    public void testInstantiation() throws Exception {
        UtilChecks check = new UtilChecks();
        Assert.assertNotNull(check);
    }

    @Test(expected = InputValidationException.class)
    public void testInvalidInputValidation() throws Exception {
        String input = "&" + UploadSessionController.ALLOWED_CHARACTERS_UPLOAD_SESSION_NAME;
        String sanitizedInput = UtilChecks.inputValidation(input, UploadSessionController.ALLOWED_CHARACTERS_UPLOAD_SESSION_NAME, "Input label", 25);
    }

    @Test
    public void testValidInputValidation() throws Exception {
        String sanitizedInput = UtilChecks.inputValidation(UploadSessionController.ALLOWED_CHARACTERS_UPLOAD_SESSION_NAME, UploadSessionController.ALLOWED_CHARACTERS_UPLOAD_SESSION_NAME, "Input label", UploadSessionController.ALLOWED_CHARACTERS_UPLOAD_SESSION_NAME.length());
        Assert.assertEquals(sanitizedInput, UploadSessionController.ALLOWED_CHARACTERS_UPLOAD_SESSION_NAME);
    }

    @Test(expected = InputValidationException.class)
    public void testEmptyInputValidation() throws Exception {
        UtilChecks.inputValidation("", UploadSessionController.ALLOWED_CHARACTERS_UPLOAD_SESSION_NAME, "Input label", 5);
    }

    @Test(expected = InputValidationException.class)
    public void testLengthInputValidation() throws Exception {
        String input = "0123456789" + "1";
        String sanitizedInput = UtilChecks.inputValidation(input, UploadSessionController.ALLOWED_CHARACTERS_UPLOAD_SESSION_NAME, "Input label", 10);
    }

    @Test
    public void testIsDate() {
        Assert.assertEquals(false, UtilChecks.isDate("01-HUP-1999"));
        Assert.assertEquals(false, UtilChecks.isDate("01-1977"));
        Assert.assertEquals(true, UtilChecks.isDate("01-08-1977"));
    }

    @Test
    public void testIsPDate() {
        Assert.assertEquals(false, UtilChecks.isPDate("01-HUP-1999"));
        Assert.assertEquals(false, UtilChecks.isPDate("Hup-1999"));
        Assert.assertEquals(false, UtilChecks.isPDate("01-1977"));
        Assert.assertEquals(false, UtilChecks.isPDate("HUP-2008"));
        Assert.assertEquals(true, UtilChecks.isPDate("Aug-2008"));
        Assert.assertEquals(true, UtilChecks.isPDate("2008"));
        Assert.assertEquals(true, UtilChecks.isPDate("08-Aug-2008"));
    }


    @Test
    public void testIsFloat() throws Exception {
        Assert.assertEquals(false, UtilChecks.isFloat("C'est ci pas un ile flottant"));
        Assert.assertEquals(false, UtilChecks.isFloat("314,0"));
        Assert.assertEquals(false, UtilChecks.isFloat("314,.0"));
        Assert.assertEquals(true, UtilChecks.isFloat("314"));

        Assert.assertEquals(false, UtilChecks.isFloat("-1.0-"));

        Assert.assertEquals(true, UtilChecks.isFloat("3.14"));
    }

    @Test
    public void testRemoveFromListIf() {
        List<Subject> subjectList = new ArrayList<>();
        Subject subject = new Subject();
        subject.setSsid("Gerrit Gerritsen");
        subject.addErrorClassification(ErrorClassification.BLOCK_ENTIRE_CRF);
        subjectList.add(subject);

        UtilChecks.removeFromListIf(subjectList, testSubject -> testSubject.getSsid().equals("Jan"));
        Assert.assertFalse(subjectList.isEmpty());

        UtilChecks.removeFromListIf(subjectList, testSubject -> testSubject.getSsid().equals("Gerrit Gerritsen"));
        Assert.assertTrue(subjectList.isEmpty());
        subjectList.add(subject);

        UtilChecks.removeFromListIf(subjectList, testSubject -> testSubject.getSsid().equals("Jan"),
                                    testSubject2 -> testSubject2.hasErrorOfType(ErrorClassification.BLOCK_ENTIRE_CRF));
        Assert.assertFalse(subjectList.isEmpty());

        UtilChecks.removeFromListIf(subjectList, testSubject -> testSubject.getSsid().equals("Gerrit Gerritsen"),
                testSubject2 -> testSubject2.hasErrorOfType(ErrorClassification.BLOCK_ENTIRE_CRF));
        Assert.assertTrue(subjectList.isEmpty());
    }


    @Test
    public void testContainsErrorOfType() {
        List<Subject> subjectList = new ArrayList<>();
        Subject subject = new Subject();
        subject.setSsid("Piet Pietersen");
        subject.addErrorClassification(ErrorClassification.BLOCK_ENTIRE_CRF);
        subjectList.add(subject);

        Assert.assertTrue(UtilChecks.listContainsErrorOfType(subjectList, ErrorClassification.BLOCK_ENTIRE_CRF));
    }


    @Test
    public void testAddErrorClassificationToAll() {
        List<Event> eventList = new ArrayList<>();
        eventList.add(new Event());
        UtilChecks.addErrorClassificationToAll(eventList, ErrorClassification.BLOCK_ENTIRE_UPLOAD);

        List<Subject> subjectList = new ArrayList<>();
        subjectList.add(new Subject());
        UtilChecks.addErrorClassificationToAll(subjectList, ErrorClassification.BLOCK_ENTIRE_UPLOAD);

        List<ClinicalData> clinicalDataList = new ArrayList<>();
        clinicalDataList.add(new ClinicalData());
        UtilChecks.addErrorClassificationToAll(clinicalDataList, ErrorClassification.BLOCK_ENTIRE_UPLOAD);

        Assert.assertTrue(UtilChecks.listContainsErrorOfType(clinicalDataList, ErrorClassification.BLOCK_ENTIRE_UPLOAD));
        Assert.assertTrue(UtilChecks.listContainsErrorOfType(subjectList, ErrorClassification.BLOCK_ENTIRE_UPLOAD));
        Assert.assertTrue(UtilChecks.listContainsErrorOfType(eventList, ErrorClassification.BLOCK_ENTIRE_UPLOAD));
    }

    @Test
    public void testAddErrorClassificationForSubjects() {
        List<Event> eventList = new ArrayList<>();
        Event event = new Event();
        event.setSsid("Jan Janssen");
        eventList.add(event);

        event = new Event();
        event.setSsid("Kees Keesen");
        eventList.add(event);

        Set<String> subjectsIDSet = new HashSet<>();
        subjectsIDSet.add("Kees Keesen");
        UtilChecks.addErrorClassificationForSubjects(eventList, subjectsIDSet, ErrorClassification.BLOCK_ENTIRE_UPLOAD);

        Event testEvent = eventList.stream().filter( searchEvent -> "Kees Keesen".equals(searchEvent.getSsid())).findAny().get();
        Assert.assertTrue(testEvent.hasErrorOfType(ErrorClassification.BLOCK_ENTIRE_UPLOAD));

        testEvent = eventList.stream().filter( searchEvent -> "Jan Janssen".equals(searchEvent.getSsid())).findAny().get();
        Assert.assertFalse(testEvent.hasErrorOfType(ErrorClassification.BLOCK_ENTIRE_UPLOAD));
    }
}
