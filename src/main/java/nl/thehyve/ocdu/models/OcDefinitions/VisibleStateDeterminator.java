/*
 * Copyright © 2016-2018 The Hyve B.V. and Netherlands Cancer Institute (NKI).
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

import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.validators.Tree;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Determines if a clinical data item is visible based on it's definition and on associated other clinical data items.
 * Created by Jacob on 2/27/18.
 */
public class VisibleStateDeterminator {

    private Map<ClinicalData, ItemDefinition> definitionMap = new LinkedHashMap<>();

    private Tree<ClinicalData> tree;


    /**
     * Create an new instance from a map of {@link ClinicalData} and {@link ItemDefinition} elements which should be
     * from one single subject/event.
     * @param clinicalDataList list of the clinica data
     * @throws IllegalStateException if the data if from a different subject / event or if the clinical data list is
     * empty/null
     *
     */
    public VisibleStateDeterminator(List<ClinicalData> clinicalDataList,
                                    Map<ClinicalData, ItemDefinition> definitionMap) {
        if ((clinicalDataList == null) || (clinicalDataList.isEmpty())) {
            throw new IllegalStateException("ClinicalData list is empty or null");
        }
        this.definitionMap = definitionMap;
        long lineNumber = clinicalDataList.get(0).getLineNumber();
        for (ClinicalData clinicalData : clinicalDataList) {
            if (lineNumber != clinicalData.getLineNumber()) {
                throw new IllegalStateException("ClinicalData list should only contain data from one subject (data from" +
                        " one line in the input file)");
            }
        }


        ClinicalData rootNode = new ClinicalData();
        tree = new Tree<>(rootNode);
        determineShowHideDepth(clinicalDataList);
        long maxLevel = 0;
        for (ClinicalData clinicalData : clinicalDataList) {
            if (clinicalData.getShowHideLevel() > maxLevel) {
                maxLevel = clinicalData.getShowHideLevel();
            }
        }

        // start building the tree

        for (long showHideDepth = 0; showHideDepth <= maxLevel; showHideDepth++) {
            for (ClinicalData clinicalData : clinicalDataList) {
                if (clinicalData.getShowHideLevel() == showHideDepth) {
                    ClinicalData controlClinicalData = findControlItem(clinicalDataList, clinicalData);
                    Tree<ClinicalData> parent;
                    if (controlClinicalData == null) {
                        parent = tree.getTree(rootNode);
                    }
                    else {
                        parent = tree.getTree(controlClinicalData);
                    }
                    if (parent != null) {
                        tree.addLeaf(parent.getHead(), clinicalData);
                    }
                }
            }
        }
    }


    private ClinicalData findControlItem(List<ClinicalData> clinicalDataList,
                                         ClinicalData clinicalData) {
        ItemDefinition itemDefinition = definitionMap.get(clinicalData);
        if (itemDefinition.getDisplayRule() == null) {
            return null;
        }
        String controlItemName = itemDefinition.getDisplayRule().getControlItemName();
        if (StringUtils.isBlank(controlItemName)) {
            return null;
        }
        ClinicalData searchClinicalData = new ClinicalData();
        searchClinicalData.setItem(controlItemName);
        searchClinicalData.setLineNumber(clinicalData.getLineNumber());
        int position = clinicalDataList.indexOf(searchClinicalData);
        if (position >= 0) {
            return clinicalDataList.get(position);
        }
        return null;
    }

    private void determineShowHideDepth(List<ClinicalData> clinicalDataList) {
        // store the ClinicalData already visited to avoid circular references. E.g. A depends on B and B depends on A.
        List<ClinicalData> visitedList = new ArrayList<>();
        for (ClinicalData clinicalData : clinicalDataList) {
            long level = 0;
            ClinicalData controlClinicalData = findControlItem(clinicalDataList, clinicalData);
            visitedList.add(clinicalData);
            while (controlClinicalData != null) {
                level++;
                if (! visitedList.contains(controlClinicalData)) {
                    visitedList.add(controlClinicalData);
                    controlClinicalData = findControlItem(clinicalDataList, controlClinicalData);
                }
                else {
                    controlClinicalData = null;
                }
            }
            if (clinicalData.getShowHideLevel() == 0) {
                clinicalData.setShowHideLevel(level + 1);
            }
        }
    }

    public Tree<ClinicalData> getTree() {
        return tree;
    }

    public boolean determineShown(ClinicalData clinicalDataToCheck, MetaData metaData) {
        ItemDefinition itemDefinition = definitionMap.get(clinicalDataToCheck);
        if (itemDefinition == null) {
            return true;
        }
        Tree<ClinicalData> treeNode = tree.getTree(clinicalDataToCheck);
        if ((treeNode == null) || (treeNode.getParent() == null)) {
            return true;
        }
        boolean shown = true;
        while (treeNode.getParent() != null) {
            ClinicalData controlClinicalData = treeNode.getParent().getHead();
            itemDefinition = definitionMap.get(clinicalDataToCheck);
            DisplayRule displayRule = itemDefinition.getDisplayRule();
            if ((displayRule == null) ||
                    (StringUtils.isBlank(displayRule.getControlItemName()))) {
                return shown;
            }
            String crfOID =
                    metaData.findFormOID(clinicalDataToCheck.getCrfName(), clinicalDataToCheck.getCrfVersion());
            if (crfOID.equals(displayRule.getAppliesInCrf())) {
                ItemDefinition controlItemDefinition = definitionMap.get(controlClinicalData);
                shown = shown && isDisplayRuleSatisfied(itemDefinition, controlItemDefinition, controlClinicalData);
            }
            clinicalDataToCheck = controlClinicalData;
            treeNode = tree.getTree(controlClinicalData);
        }
        return shown;
    }

    private Boolean isDisplayRuleSatisfied(ItemDefinition itemDefinition, ItemDefinition controlItemDefinition, ClinicalData clinicalData) {
        DisplayRule displayRule = itemDefinition.getDisplayRule();
        if ((displayRule == null) ||
                (controlItemDefinition == null)) {
            // i.e. no display-rule defined for the item, then it is per definition visible
            return true;
        }
        String optionValue = displayRule.getOptionValue();
        if (StringUtils.isBlank(optionValue)) {
            return true;
        }
        if (controlItemDefinition.isMultiselect()) {
            List<String> values = clinicalData.getValues(true);
            if (values.contains(optionValue)) {
                return true;
            }
            return false;
        }
        else {
            return optionValue.equals(clinicalData.getValue());
        }
    }
}
