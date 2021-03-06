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

import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.util.*;

/**
 * Created by piotrzakrzewski on 01/05/16.
 */
@Entity
public class MetaData {


    /**
     * OpenClinica code for the study option 'Collect subject Date of Birth; as a full date
     */
    public static final int BIRTH_DATE_AS_FULL_DATE = 1;

    /**
     * OpenClinica code for the study option 'Collect subject Date of Birth; as only the year of birth
     */
    public static final int BIRTH_DATE_AS_ONLY_YEAR = 2;

    /**
     * OpenClinica code for the study option 'Collect subject Date of Birth; date of birth not used
     */
    public static final int BIRTH_DATE_NOT_USED = 3;


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String studyOID;

    private String studyName;

    @OneToMany(targetEntity = EventDefinition.class, cascade = CascadeType.ALL)
    private List<EventDefinition> eventDefinitions;

    @OneToMany(targetEntity = ItemGroupDefinition.class, cascade = CascadeType.ALL)
    private List<ItemGroupDefinition> itemGroupDefinitions;

    @OneToMany(targetEntity = CodeListDefinition.class, cascade = CascadeType.ALL)
    private List<CodeListDefinition> codeListDefinitions;

    @OneToMany(targetEntity = SiteDefinition.class, cascade = CascadeType.ALL)
    private List<SiteDefinition> siteDefinitions = new ArrayList<>();
    private String status;
    private String protocolName;

    private SubjectIDGeneration subjectIDGeneration;

    public void addEventDefinition(EventDefinition eventDef) {
        eventDefinitions.add(eventDef);
    }

    public void removeEventDefinition(EventDefinition eventDef) {
        eventDefinitions.remove(eventDef);
    }


    public void addItemGroupDefinition(ItemGroupDefinition itemGroupDef) {
        itemGroupDefinitions.add(itemGroupDef);
    }

    public void removeItemGroupDefinition(ItemGroupDefinition itemGroupDef) {
        itemGroupDefinitions.remove(itemGroupDef);
    }

    public void addCodeListDefinition(CodeListDefinition codeListDefinition) {
        codeListDefinitions.add(codeListDefinition);
    }

    public void removeCodeListDefinition(CodeListDefinition codeListDefinition) {
        codeListDefinitions.remove(codeListDefinition);
    }

    /**
     * Returns a clinical data form's OID based on the name and version.
     * @param crfName the form name
     * @param crfVersion the form version
     * @return an empty {@link String} if the crfName or crfVersion are empty or if
     * the form's OID can not be found. In other cases it returns the form's OID.
     */
    public String findFormOID(String crfName, String crfVersion) {
        if ((StringUtils.isEmpty(crfName)) ||
                (StringUtils.isEmpty(crfVersion))) {
            return "";
        }
        for (EventDefinition eventDefinition : eventDefinitions) {
            for (CRFDefinition crfDefinition : eventDefinition.getCrfDefinitions()) {
                if ((crfName.equals(crfDefinition.getName())) &&
                    (crfVersion.equals(crfDefinition.getVersion()))) {
                    return crfDefinition.getOid();
                }
            }
        }
        return "";
    }


    /**
     * Created a map with the formOID as key and the list of item names used in that form.
     * @return
     */
    public Map<String, Set<String>> obtainFormOIDItemNameMap() {
        Map<String, Set<String>> crfVersionItemNameMap = new HashMap<>();
        for (EventDefinition eventDefinition : eventDefinitions) {
            for (CRFDefinition crfDefinition : eventDefinition.getCrfDefinitions()) {
                String formOID = crfDefinition.getOid();
                Set<String> formItemNameSet = crfVersionItemNameMap.get(formOID);
                if (formItemNameSet == null) {
                    formItemNameSet = new HashSet<>();
                    crfVersionItemNameMap.put(formOID, formItemNameSet);
                }
                List<ItemDefinition> itemDefinitionList = crfDefinition.getUngroupedItems();
                for (ItemDefinition itemDefinition : itemDefinitionList) {
                    formItemNameSet.add(itemDefinition.getName());
                }
            }
        }
        return crfVersionItemNameMap;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getStudyOID() {
        return studyOID;
    }

    public void setStudyOID(String studyOID) {
        this.studyOID = studyOID;
    }

    public List<EventDefinition> getEventDefinitions() {
        return eventDefinitions;
    }

    public void setEventDefinitions(List<EventDefinition> eventDefinitions) {
        this.eventDefinitions = eventDefinitions;
    }

    public List<ItemGroupDefinition> getItemGroupDefinitions() {
        return itemGroupDefinitions;
    }

    public void setItemGroupDefinitions(List<ItemGroupDefinition> itemGroupDefinitions) {
        this.itemGroupDefinitions = itemGroupDefinitions;
    }

    public List<CodeListDefinition> getCodeListDefinitions() {
        return codeListDefinitions;
    }

    public void setCodeListDefinitions(List<CodeListDefinition> codeListDefinitions) {
        this.codeListDefinitions = codeListDefinitions;
    }

    public List<SiteDefinition> getSiteDefinitions() {
        return siteDefinitions;
    }

    private boolean genderRequired;

    private ProtocolFieldRequirementSetting personIDUsage;

    private int birthdateRequired;

    public ProtocolFieldRequirementSetting getPersonIDUsage() {
        return personIDUsage;
    }

    public void setPersonIDUsage(ProtocolFieldRequirementSetting personIDUsage) {
        this.personIDUsage = personIDUsage;
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

    public void setSiteDefinitions(List<SiteDefinition> siteDefinitions) {
        this.siteDefinitions = siteDefinitions;
    }

    public String getStudyName() {
        return studyName;
    }

    public void setStudyName(String studyName) {
        this.studyName = studyName;
    }

    public MetaData() {
        this.codeListDefinitions = new ArrayList<>();
        this.itemGroupDefinitions = new ArrayList<>();
        this.eventDefinitions = new ArrayList<>();
    }


    /**
     * Returns the appropriate {@link EventDefinition} based on an Eventname. Can return <code>null</code>
     * @param eventName
     * @return
     */
    public EventDefinition findEventDefinitionByName(String eventName) {
        Optional<EventDefinition> matchingEventDefintion =
                eventDefinitions.stream().filter(eventDefinition -> eventDefinition.getName().equalsIgnoreCase(eventName)).findAny();
        if (matchingEventDefintion.isPresent()) {
            return matchingEventDefintion.get();
        }
        // we return null and do not throw and exception for the case the event name specified in
        // the input file does not exist.
        return null;
    }

    public String findEventOID(String eventName) {
        Optional<EventDefinition> matchingEventDefintion =
                eventDefinitions.stream().filter(eventDefinition -> eventDefinition.getName().equals(eventName)).findAny();
        if (matchingEventDefintion.isPresent()) {
            return matchingEventDefintion.get().getStudyEventOID();
        }
        throw new IllegalStateException("No matching eventOID found for event with name " + eventName);
    }

    public String findEventName(String eventOID) {
        Optional<EventDefinition> matchingEventDefintion =
                eventDefinitions.stream().filter(eventDefinition -> eventDefinition.getStudyEventOID().equals(eventOID)).findAny();
        if (matchingEventDefintion.isPresent()) {
            return matchingEventDefintion.get().getName();
        }
        throw new IllegalStateException("No matching event name found for event with OID " + eventOID);
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setLocationRequirementSetting(ProtocolFieldRequirementSetting protocolFieldRequirementSetting) {
        this.locationRequirementSetting = protocolFieldRequirementSetting;
    }

    public SubjectIDGeneration getSubjectIDGeneration() {
        return subjectIDGeneration;
    }

    public void setSubjectIDGeneration(SubjectIDGeneration subjectIDGeneration) {
        this.subjectIDGeneration = subjectIDGeneration;
    }

    public ProtocolFieldRequirementSetting getLocationRequirementSetting() {
        return locationRequirementSetting;
    }

    private ProtocolFieldRequirementSetting locationRequirementSetting = ProtocolFieldRequirementSetting.OPTIONAL;

    public void setProtocolName(String protocolName) {
        this.protocolName = protocolName;
    }

    public String getProtocolName() {
        return protocolName;
    }


}
