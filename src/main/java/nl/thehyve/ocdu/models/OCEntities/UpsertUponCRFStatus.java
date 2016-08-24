package nl.thehyve.ocdu.models.OCEntities;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Specifies the CRF status upon which data upload or data overwriting is allowed
 * Created by jacob on 8/24/16.
 */
public enum UpsertUponCRFStatus {
    NOT_STARTED("notStarted"),
    DATA_ENTRY_STARTED("dataEntryStarted"),
    DATA_ENTRY_COMPLETE("dataEntryComplete");

    private String value;
    private boolean upsert;

    UpsertUponCRFStatus(String value) {
        this.value = value;
    }

    public String getStringValue() {
        return value;
    }

    private static final Map<String, UpsertUponCRFStatus> lookup = new HashMap<>();

    static {
        for(UpsertUponCRFStatus upsertUponCRFStatus : EnumSet.allOf(UpsertUponCRFStatus.class)) {
            lookup.put(upsertUponCRFStatus.value, upsertUponCRFStatus);
        }
    }

    public static UpsertUponCRFStatus lookupByValue(String lookupValue) {
        return lookup.get(lookupValue);
    }
}
