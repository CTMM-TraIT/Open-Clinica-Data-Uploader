package nl.thehyve.ocdu.validators;

import nl.thehyve.ocdu.controllers.UploadSessionController;
import nl.thehyve.ocdu.services.InputValidationException;
import org.junit.Assert;

import org.junit.Test;

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
    public void testisFloat() throws Exception {
        Assert.assertEquals(false, UtilChecks.isFloat("C'est ci pas un ile flottant"));
        Assert.assertEquals(false, UtilChecks.isFloat("314,0"));
        Assert.assertEquals(false, UtilChecks.isFloat("314,.0"));
        Assert.assertEquals(false, UtilChecks.isFloat("314"));

        Assert.assertEquals(false, UtilChecks.isFloat("-1.0-"));

        Assert.assertEquals(true, UtilChecks.isFloat("3.14"));
    }
}
