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

import nl.thehyve.ocdu.validators.UtilChecks;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

/**
 * Created by piotrzakrzewski on 01/05/16.
 */
@Entity
public class CodeListDefinition {

    private static final int MAXIMAL_SCALE = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String ocid;

    public String getOcid() {
        return ocid;
    }

    public void setOcid(String ocid) {
        this.ocid = ocid;
    }

    @OneToMany(targetEntity = CodeListItemDefinition.class)
    private List<CodeListItemDefinition> items = new ArrayList<>();

    public List<CodeListItemDefinition> getItems() {
        return items;
    }

    public void setItems(List<CodeListItemDefinition> items) {
        this.items = items;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public boolean isAllowed(String value, String expectedType) {
        if (UtilChecks.FLOAT_DATA_TYPE.equals(expectedType)) {
            if (StringUtils.isBlank(value)) {
                return false;
            }
            BigDecimal valueToCheck;
            try {
                valueToCheck = new BigDecimal(value.trim());
            }
            catch (NumberFormatException nfe) {
                return false;
            }

            for (CodeListItemDefinition codeListItemDefinition : items) {
                String strValue = codeListItemDefinition.getContent();
                if (value.trim().equals(strValue.trim())) {
                    return true;
                }
            }
            return false;
        }
        return items.stream()
                .anyMatch(codeListItemDefinition -> codeListItemDefinition.getContent().equals(value));
    }

    @Override
    public String toString() {
        return items.toString();
    }
}
