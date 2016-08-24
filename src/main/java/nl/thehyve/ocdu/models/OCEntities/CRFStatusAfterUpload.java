package nl.thehyve.ocdu.models.OCEntities;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jacob on 8/23/16.
 */
public enum CRFStatusAfterUpload {
    INITIAL_DATA_ENTRY("initial data entry"),
    DATA_ENTRY_CONPLETED("complete");

    private String value;

    CRFStatusAfterUpload(String value) {
        this.value = value;
    }

    private static final Map<String, CRFStatusAfterUpload> lookup = new HashMap<>();

    static {
        for(CRFStatusAfterUpload crfStatusAfterUpload : EnumSet.allOf(CRFStatusAfterUpload.class)) {
            lookup.put(crfStatusAfterUpload.value, crfStatusAfterUpload);
        }
    }

    public static CRFStatusAfterUpload lookupByValue(String lookupValue) {
        return lookup.get(lookupValue);
    }
}
