package nl.thehyve.ocdu.models.OcDefinitions;

import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Created by jacob on 8/26/16.
 */
public class ClinicalDataTest {

    @Test
    public void testConvertValueToISO_8601() {
        ClinicalData clinicalData = new ClinicalData();
        clinicalData.setValue("26-08-2016");

        clinicalData.convertValueToISO_8601();

        assertEquals("2016-08-26", clinicalData.getValue());
    }
}
