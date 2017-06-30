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
