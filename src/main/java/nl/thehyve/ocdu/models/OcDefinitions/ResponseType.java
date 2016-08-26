package nl.thehyve.ocdu.models.OcDefinitions;


import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * An enumeration of the repsonse types OpenClinica supports.
 * Created by jacob on 8/26/16.
 */
public enum ResponseType {

    INVALID("invalid"),
    TEXT("text"),
    TEXTAREA("textarea"),
    CHECKBOX("checkbox"),
    FILE("file"),
    RADIO("radio"),
    SINGLE_SELECT("single-select"),
    MULTIPLE_SELECT("multi-select"),
    CALCULATION("calculation"),
    GROUP_CALCULATION("group-calculation"),
    INSTANT_CALCULATION("instant-calculation");

    private String description;

    private static final Map<String, ResponseType> lookup = new HashMap<>();

    static {
        for(ResponseType pc : EnumSet.allOf(ResponseType.class)) {
            lookup.put(pc.getDescription(), pc);
        }
    }

    ResponseType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static ResponseType lookupByDescription(String searchKey) {
        return lookup.get(searchKey);
    }
}
