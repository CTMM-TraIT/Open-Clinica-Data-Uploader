package nl.thehyve.ocdu.nl.thehyve.ocdu.models.OCEntities;

import nl.thehyve.ocdu.models.OCEntities.Subject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for the {@link nl.thehyve.ocdu.models.OCEntities.Subject} class.
 * Created by jacob on 10/13/16.
 */
public class SubjectTests {

    @Test
    public void testCheckLeadingZerosInDates() {
        Subject subject = new Subject();
        subject.setDateOfBirth("2-1-2005");
        subject.addLeadingZerosInDates();
        assertEquals("02-01-2005", subject.getDateOfBirth());

        subject.setDateOfEnrollment("9-9-2016");
        subject.addLeadingZerosInDates();
        assertEquals("09-09-2016", subject.getDateOfEnrollment());
    }
}
