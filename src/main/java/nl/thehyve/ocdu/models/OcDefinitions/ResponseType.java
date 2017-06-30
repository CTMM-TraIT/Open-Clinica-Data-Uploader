/*
 * Copyright © 2016-2017 The Hyve B.V. and Netherlands Cancer Institute (NKI).
 *
 * This file is part of OCDI (OpenClinica Data Importer).
 *
 * OCDI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OCDI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OCDI. If not, see <http://www.gnu.org/licenses/>.
 */

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
