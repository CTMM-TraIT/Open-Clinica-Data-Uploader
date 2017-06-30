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

import javax.persistence.*;
import java.util.List;

/**
 * Created by piotrzakrzewski on 07/06/16.
 */
@Entity
public class DisplayRule {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String controlItemName; // name of the item which value has to be checked against optionValue
    private String appliesInCrf;  // OIDs of CRF to which the DisplayRule apply
    private String optionValue;  // if controlItemName's value equals optionValue, then the item is shown
    private boolean show;

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getControlItemName() {
        return controlItemName;
    }

    public void setControlItemName(String controlItemName) {
        this.controlItemName = controlItemName;
    }

    public String getAppliesInCrf() {
        return appliesInCrf;
    }

    public void setAppliesInCrf(String appliesInCrf) {
        this.appliesInCrf = appliesInCrf;
    }

    public String getOptionValue() {
        return optionValue;
    }

    public void setOptionValue(String optionValue) {
        this.optionValue = optionValue;
    }
}
