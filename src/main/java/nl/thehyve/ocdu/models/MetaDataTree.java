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

package nl.thehyve.ocdu.models;

import nl.thehyve.ocdu.services.DataService;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple tree implementation for purpose of mapping view (currently not used, as only simple list to list mapping
 * is employed at the moment).
 *
 * Created by piotrzakrzewski on 21/06/16.
 */
public class MetaDataTree {
    private String name;
    //private MetaDataTree parent;
    private List<MetaDataTree> children = new ArrayList<>();

    public MetaDataTree(String name) {
        this.name = name;
    }

    public MetaDataTree() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MetaDataTree> getChildren() {
        return children;
    }

    public void setChildren(List<MetaDataTree> children) {
        this.children = children;
    }

    public void addChild(MetaDataTree node) {
        this.children.add(node);
    }
}
