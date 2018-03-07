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
import java.util.Optional;

/**
 * Entity for the item definition, present in metadata returned by OpenClinica.
 * Created by piotrzakrzewski on 01/05/16.
 */
@Entity
public class ItemDefinition implements ODMElement {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String oid;
    private String name;
    private String dataType;
    private int length;
    private boolean mandatoryInGroup = false;
    private boolean isMultiselect = false;
    private String codeListRef;
    private ResponseType responseType;

    @OneToOne(targetEntity = DisplayRule.class)
    private DisplayRule displayRule = new DisplayRule();

    @OneToMany(targetEntity = ItemPresentInForm.class)
    private List<ItemPresentInForm> itemPresentInFormList = new ArrayList<>();

    private int significantDigits = 0;
    @OneToMany(targetEntity = RangeCheck.class)
    private List<RangeCheck> rangeCheckList;
    @OneToOne(targetEntity = ItemGroupDefinition.class)
    private ItemGroupDefinition group;

    public ItemDefinition() {
    }

    public ItemDefinition(ItemDefinition prototype) {
        this.name = prototype.getName();
        this.length = prototype.getLength();
        this.oid = prototype.getOid();
        this.mandatoryInGroup = prototype.isMandatoryInGroup();
        this.itemPresentInFormList = prototype.getItemPresentInFormList();
        this.dataType = prototype.getDataType();
        this.rangeCheckList = prototype.getRangeCheckList();
        this.significantDigits = prototype.getSignificantDigits();
        this.isMultiselect = prototype.isMultiselect();
        this.codeListRef = prototype.getCodeListRef();
        this.displayRule = prototype.getDisplayRule();
        this.group = prototype.getGroup();
        this.responseType = prototype.getResponseType();
    }

    public List<ItemPresentInForm> getItemPresentInFormList() {
        return itemPresentInFormList;
    }

    public void setItemPresentInFormList(List<ItemPresentInForm> itemPresentInFormList) {
        this.itemPresentInFormList = itemPresentInFormList;
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }

    public DisplayRule getDisplayRule() {
        return displayRule;
    }

    public void setDisplayRule(DisplayRule displayRule) {
        this.displayRule = displayRule;
    }

    public String getCodeListRef() {
        return codeListRef;
    }

    public void setCodeListRef(String codeListRef) {
        this.codeListRef = codeListRef;
    }

    public boolean isMultiselect() {
        return isMultiselect;
    }

    public void setMultiselect(boolean multiselect) {
        isMultiselect = multiselect;
    }

    public int getSignificantDigits() {
        return significantDigits;
    }

    public void setSignificantDigits(int significantDigits) {
        this.significantDigits = significantDigits;
    }

    public boolean isMandatoryInGroup() {
        return mandatoryInGroup;
    }

    public void setMandatoryInGroup(boolean mandatoryInGroup) {
        this.mandatoryInGroup = mandatoryInGroup;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<RangeCheck> getRangeCheckList() {
        return rangeCheckList;
    }

    public void setRangeCheckList(List<RangeCheck> rangeCheckList) {
        this.rangeCheckList = rangeCheckList;
    }

    public boolean isRepeating() {
        if (group == null) {
            return false; // ungrouped items cannot be repeating
        } else {
            return group.isRepeating();
        }
    }

    public ItemGroupDefinition getGroup() {
        return group;
    }

    public void setGroup(ItemGroupDefinition group) {
        this.group = group;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemDefinition that = (ItemDefinition) o;

        return oid != null ? oid.equals(that.oid) : that.oid == null;

    }

    public boolean isRequiredInCRF(String formOID) {
        Optional<ItemPresentInForm> itemPresentInFormOptional =
                itemPresentInFormList.stream().filter( itemPresentInFormSearch -> itemPresentInFormSearch.getFormOID().equals(formOID)).findFirst();
        if (itemPresentInFormOptional.isPresent()) {
            ItemPresentInForm itemPresentInForm = itemPresentInFormOptional.get();
            return itemPresentInForm.isRequired();
        }
        throw new IllegalStateException("Unable to determine the requiredness of item " + this.getName() + "," + this.getOid());
    }

    @Override
    public int hashCode() {
        return oid != null ? oid.hashCode() : 0;
    }
}
