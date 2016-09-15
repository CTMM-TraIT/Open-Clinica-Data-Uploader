package nl.thehyve.ocdu.nl.thehyve.ocdu.models.OCEntities;

import nl.thehyve.ocdu.models.Mapping;
import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.UploadSession;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

/**
 * Created by jacob on 9/15/16.
 */
public class MappingTests {

    @Test
    public void testMappingToYaml() {
        UploadSession uploadSession = new UploadSession();
        uploadSession.setName("StudyXYZUpload");
        uploadSession.setUponDataEntryCompleted(false);
        uploadSession.setUponDataEntryStarted(true);
        uploadSession.setUponNotStarted(true);
        uploadSession.setStudy("Study_XYZ");
        uploadSession.setCrfStatusAfterUpload("Marked Complete");

        Map<String, String> map = new HashMap<>();
        map.put("Hallo", "World");
        map.put("Nou tabe dan, ", "mijn mooi Amsterdam");

        Mapping mapping = new Mapping(map, uploadSession);

        String result = mapping.toYaml();

        assertThat(result, containsString("mijn mooi Amsterdam\n"));
    }
}
