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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by piotrzakrzewski on 01/05/16.
 */
@Entity
public class EventDefinition implements ODMElement {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @OneToMany(targetEntity = CRFDefinition.class)
    private List<CRFDefinition> crfDefinitions;

    private String studyEventOID;
    private String name;
    private int orderNumber;
    private boolean mandatory;
    private boolean repeating;
    private String type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRepeating() {
        return repeating;
    }

    public void setRepeating(boolean repeating) {
        this.repeating = repeating;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getStudyEventOID() {
        return studyEventOID;
    }

    public void setStudyEventOID(String studyEventOID) {
        this.studyEventOID = studyEventOID;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public List<CRFDefinition> getCrfDefinitions() {
        return crfDefinitions;
    }

    public void setCrfDefinitions(List<CRFDefinition> crfDefinitions) {
        this.crfDefinitions = crfDefinitions;
    }

    public void addCrfDef(CRFDefinition crfDefinition) {
        this.crfDefinitions.add(crfDefinition);
    }

    public void removeCrfDef(CRFDefinition crfDefinition) {
        this.crfDefinitions.remove(crfDefinition);
    }

    public EventDefinition() {
        this.crfDefinitions = new ArrayList<>();
    }
}
