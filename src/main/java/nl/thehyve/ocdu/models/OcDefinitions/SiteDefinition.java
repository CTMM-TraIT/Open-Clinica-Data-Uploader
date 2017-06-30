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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Created by bo on 6/8/16.
 */
@Entity
public class SiteDefinition {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String siteOID;

    private String name;

    private String uniqueID;

    private boolean genderRequired = true;
    //TODO: refactor birthdateRequired into Enumeration
    /*
     * 1. yes, required
     * 2. only year of birth
     * 3. not required
     */
    private int birthdateRequired = 1;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isGenderRequired() {
        return genderRequired;
    }

    public void setGenderRequired(boolean genderRequired) {
        this.genderRequired = genderRequired;
    }

    public int getBirthdateRequired() {
        return birthdateRequired;
    }

    public void setBirthdateRequired(int birthdateRequired) {
        this.birthdateRequired = birthdateRequired;
    }

    public String getSiteOID() {
        return siteOID;
    }



    public void setSiteOID(String siteOID) {
        this.siteOID = siteOID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }
}
