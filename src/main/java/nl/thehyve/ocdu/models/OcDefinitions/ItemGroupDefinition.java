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
 * Entity for the item group definition, present in metadata returned by OpenClinica.
 * Created by piotrzakrzewski on 01/05/16.
 */
@Entity
public class ItemGroupDefinition implements ODMElement {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private boolean repeating;

    private String oid;

    private String name;


    private boolean ungrouped = false;

    public boolean isUngrouped() {
        return ungrouped;
    }

    public void setUngrouped(boolean ungrouped) {
        this.ungrouped = ungrouped;
    }

    public List<String> getMandatoryItems() {
        return mandatoryItems;
    }

    public void setMandatoryItems(List<String> mandatoryItems) {
        this.mandatoryItems = mandatoryItems;
    }

    @Transient
    private List<String> mandatoryItems = new ArrayList<>();

    public boolean isMandatoryInCrf() {
        return true; // In OpenClinica interpretation of the ODM all ItemGroups are mandatory
    }


    @OneToMany(targetEntity = ItemDefinition.class, cascade = CascadeType.ALL)
    private List<ItemDefinition> items = new ArrayList<>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<ItemDefinition> getItems() {
        return items;
    }

    public void setItems(List<ItemDefinition> items) {
        this.items = items;
    }

    public boolean isRepeating() {
        return repeating;
    }

    public void setRepeating(boolean repeating) {
        this.repeating = repeating;
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

    public ItemGroupDefinition() {
    }

    public ItemGroupDefinition(ItemGroupDefinition prototype) {
        this.repeating = prototype.isRepeating();
        this.name = prototype.getName();
        this.oid = prototype.getOid();
        this.items = new ArrayList<>(prototype.getItems());
        this.mandatoryItems = prototype.getMandatoryItems();
        this.ungrouped = prototype.isUngrouped();
    }

    public void addItem(ItemDefinition item) {
        this.items.add(item);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemGroupDefinition that = (ItemGroupDefinition) o;

        return oid != null ? oid.equals(that.oid) : that.oid == null;

    }

    @Override
    public int hashCode() {
        return oid != null ? oid.hashCode() : 0;
    }
}
