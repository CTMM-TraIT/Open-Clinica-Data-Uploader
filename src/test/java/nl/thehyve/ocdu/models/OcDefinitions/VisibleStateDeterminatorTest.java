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
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

/**
 * Test class for {@link VisibleStateDeterminator}.
 * Created by jacob on 2/28/18.
 */
public class VisibleStateDeterminatorTest {


    List<ClinicalData> clinicalDataList;

    Map<ClinicalData, ItemDefinition> definitionMap;

    @Before
    public void init() {
        clinicalDataList = new ArrayList<>();
        definitionMap = new HashMap<>();
    }

    @Test(expected = IllegalStateException.class)
    public void testMultipleSourceLines() {
        ClinicalData clinicalData = buildClinicalData("A", "B");
        clinicalData.setLineNumber(0);
        clinicalDataList.add(clinicalData);
        clinicalData = buildClinicalData("B", "null");
        // use different line number than above
        clinicalData.setLineNumber(1);
        clinicalDataList.add(clinicalData);

        VisibleStateDeterminator visibleStateDeterminator = new VisibleStateDeterminator(clinicalDataList, definitionMap);
    }

    @Test(expected = IllegalStateException.class)
    public void testEmptyMap() {
        VisibleStateDeterminator visibleStateDeterminator = new VisibleStateDeterminator(clinicalDataList, definitionMap);
    }


    @Test
    public void testMissingControlItem() {
        ClinicalData clinicalData = buildClinicalData("A", "B");
        clinicalDataList.add(clinicalData);

        // Clinicaldata "D" missing.
        clinicalData = buildClinicalData("B", "D");
        clinicalDataList.add(clinicalData);

        VisibleStateDeterminator visibleStateDeterminator = new VisibleStateDeterminator(clinicalDataList, definitionMap);
        Tree<ClinicalData> tree = visibleStateDeterminator.getTree();
    }

    @Test
    public void testCircularReferenceTwoSteps() {
        ClinicalData clinicalData = buildClinicalData("A", "B");
        clinicalDataList.add(clinicalData);

        clinicalData = buildClinicalData("B", "A");
        clinicalDataList.add(clinicalData);

        VisibleStateDeterminator visibleStateDeterminator = new VisibleStateDeterminator(clinicalDataList, definitionMap);
        Tree<ClinicalData> tree = visibleStateDeterminator.getTree();
    }

    @Test
    public void testCircularReferenceMultipleSteps() {
        // A depends on B depends on C depends on-> A
        ClinicalData clinicalData = buildClinicalData("A", "B");
        clinicalDataList.add(clinicalData);

        clinicalData = buildClinicalData("B", "C");
        clinicalDataList.add(clinicalData);

        clinicalData = buildClinicalData("C", "A");
        clinicalDataList.add(clinicalData);

        VisibleStateDeterminator visibleStateDeterminator = new VisibleStateDeterminator(clinicalDataList, definitionMap);
        Tree<ClinicalData> tree = visibleStateDeterminator.getTree();
    }

    @Test
    public void testConstruction() {
        /* build the following tree
             ROOT
             /\  \
            D  B  E
           / \     \
          C   G     F
               \
                A
        */
        ClinicalData clinicalData = buildClinicalData("A", "G");
        clinicalDataList.add(clinicalData);

        clinicalData = buildClinicalData("C", "D");
        clinicalDataList.add(clinicalData);

        clinicalData = buildClinicalData("D", null);
        clinicalDataList.add(clinicalData);

        clinicalData = buildClinicalData("B", null);
        clinicalDataList.add(clinicalData);

        clinicalData = buildClinicalData("E", null);
        clinicalDataList.add(clinicalData);

        clinicalData = buildClinicalData("F", "E");
        clinicalDataList.add(clinicalData);

        clinicalData = buildClinicalData("G", "D");
        clinicalDataList.add(clinicalData);

        VisibleStateDeterminator visibleStateDeterminator = buildTestVisibleStateDeterminator();

        Tree<ClinicalData> tree = visibleStateDeterminator.getTree();

        List<String> levelOneList = new ArrayList();
        levelOneList.add("D");
        levelOneList.add("B");
        levelOneList.add("E");

        List<String> levelTwoList = new ArrayList();
        levelTwoList.add("G");
        levelTwoList.add("C");
        levelTwoList.add("F");

        Collection<Tree<ClinicalData>> leafList = tree.getSubTrees();
        for (Tree<ClinicalData> leaf : leafList) {
            ClinicalData leafClinicalData = leaf.getHead();
            ItemDefinition itemDefinition = definitionMap.get(leafClinicalData);
            String itemDefName = itemDefinition.getName();
            assertThat(levelOneList, hasItem(itemDefName));
            assertEquals(leaf.getParent().getParent(), null);
            Collection<Tree<ClinicalData>> nextLevelList = leaf.getSubTrees();
            for (Tree<ClinicalData> levelTwoNode : nextLevelList) {
                ClinicalData levelTwoClinicalData = levelTwoNode.getHead();
                itemDefName = definitionMap.get(levelTwoClinicalData).getName();
                assertThat(levelTwoList, hasItem(itemDefName));
                if (("C".equals(itemDefName)) || ("G".equals(itemDefName))) {
                    ClinicalData parentItem = levelTwoNode.getParent().getHead();
                    assertThat(parentItem.getItem(), is("D"));
                }
            }
        }
    }


    @Test
    public void testDetermineShown() {
        /* build the following tree; + indicates visible and - invisible
             ROOT +
             /\  \
            D+ B+ E-
           / \     \
          C+ G-     F
               \
               A
        */
        ClinicalData clinicalData = buildClinicalData("A", "G");
        ItemDefinition itemDefinition = definitionMap.get(clinicalData);
        itemDefinition.getDisplayRule().setOptionValue("-");
        clinicalDataList.add(clinicalData);

        clinicalData = buildClinicalData("C", "D");
        clinicalDataList.add(clinicalData);

        clinicalData = buildClinicalData("D", null);
        clinicalDataList.add(clinicalData);

        clinicalData = buildClinicalData("B", null);
        clinicalDataList.add(clinicalData);

        clinicalData = buildClinicalData("E", null);
        clinicalDataList.add(clinicalData);

        clinicalData = buildClinicalData("F", "E");
        clinicalDataList.add(clinicalData);

        clinicalData = buildClinicalData("G", "D");
        clinicalData.setValue("-"); // i.e. A is invisible
        clinicalDataList.add(clinicalData);


        VisibleStateDeterminator visibleStateDeterminator = buildTestVisibleStateDeterminator();
        MetaData metaData = new MetaData();


        ClinicalData controlClinicalData = clinicalDataList.stream().
                filter(clinicalData1 -> "G".
                        equals(clinicalData1.getItem())).
                findFirst().get();

        ClinicalData topLevelClinicalData = clinicalDataList.stream().
                filter(clinicalData1 -> "D".
                        equals(clinicalData1.getItem())).
                findFirst().get();
        topLevelClinicalData.setValue("SomeValue");

        String appliesInCrfOID =
                metaData.findFormOID(controlClinicalData.getCrfName(), controlClinicalData.getCrfVersion());
        controlClinicalData.setValue("ValueWhichHidesA");
        itemDefinition = definitionMap.get(controlClinicalData);
        itemDefinition.getDisplayRule().setAppliesInCrf(appliesInCrfOID);

        ClinicalData clinicalDataToTest = clinicalDataList.stream().
                filter(clinicalData1 -> "A".
                equals(clinicalData1.getItem())).
                findFirst().get();

        appliesInCrfOID =
                metaData.findFormOID(clinicalDataToTest.getCrfName(), clinicalDataToTest.getCrfVersion());
        itemDefinition = definitionMap.get(clinicalDataToTest);
        itemDefinition.getDisplayRule().setControlItemName("G");
        itemDefinition.getDisplayRule().setShow(false);
        itemDefinition.getDisplayRule().setOptionValue("ValueWhichHidesA");
        itemDefinition.getDisplayRule().setAppliesInCrf(appliesInCrfOID);

        boolean visible =
                visibleStateDeterminator.determineShown(clinicalDataToTest, metaData);
        assertFalse(visible);

        controlClinicalData.setValue("ValueWhichIsNotCoveredByTheDisplayRule");
        visible =
                visibleStateDeterminator.determineShown(clinicalDataToTest, metaData);
        assertFalse(visible);

        // Item B is just below the root and is per default always visible
        clinicalDataToTest = clinicalDataList.stream().
                filter(clinicalData1 -> "B".
                equals(clinicalData1.getItem())).
                findFirst().get();
        visible =
                visibleStateDeterminator.determineShown(clinicalDataToTest, metaData);
        assertTrue(visible);

        // Item F depends on E
        topLevelClinicalData = clinicalDataList.stream().
                filter(clinicalData1 -> "E".
                        equals(clinicalData1.getItem())).
                findFirst().get();
        itemDefinition = definitionMap.get(topLevelClinicalData);
        itemDefinition.getDisplayRule().setAppliesInCrf(appliesInCrfOID);
        clinicalDataToTest = clinicalDataList.stream().
                filter(clinicalData1 -> "F".
                        equals(clinicalData1.getItem())).
                findFirst().get();
        visible =
                visibleStateDeterminator.determineShown(clinicalDataToTest, metaData);
        assertTrue(visible);
    }

    @Test
    public void testMultipleSelect() {
        /*****
         Build the following tree; where C is a multiple select
             ROOT+
             /\
            A+ B+
           /
          C+
        *******/
        ClinicalData clinicalData = buildClinicalData("C", "A");
        ItemDefinition itemDefinition = definitionMap.get(clinicalData);
        itemDefinition.getDisplayRule().setOptionValue("-");
        clinicalDataList.add(clinicalData);

        clinicalData = buildClinicalData("B", null);
        clinicalDataList.add(clinicalData);

        ClinicalData controlClinicalData = buildClinicalData("A", null);
        controlClinicalData.setValue("ValueWhichHidesC");
        clinicalDataList.add(controlClinicalData);


        MetaData metaData = new MetaData();


        VisibleStateDeterminator visibleStateDeterminator = buildTestVisibleStateDeterminator();

        clinicalData = clinicalDataList.stream().
                filter(clinicalData1 -> "C".
                        equals(clinicalData1.getItem())).
                findFirst().get();
        String appliesInCrfOID =
                metaData.findFormOID(clinicalData.getCrfName(), clinicalData.getCrfVersion());


        itemDefinition.setMultiselect(true);
        clinicalData.setValue("Value_of_C");
        itemDefinition = definitionMap.get(clinicalData);
        itemDefinition.getDisplayRule().setShow(true);
        itemDefinition.getDisplayRule().setControlItemName("A");
        itemDefinition.getDisplayRule().setOptionValue("ValueWhichShowsC");
        itemDefinition.getDisplayRule().setAppliesInCrf(appliesInCrfOID);


        boolean visible =
                visibleStateDeterminator.determineShown(clinicalData, metaData);
        assertFalse(visible);

        controlClinicalData.setValue("ValueWhichShowsC");
        visible =
                visibleStateDeterminator.determineShown(clinicalData, metaData);
        assertTrue(visible);
    }


    @Test
    public void testInvalidStates() {
        /*****
         Build the following tree
          ROOT
          /\
         A  B
        /
       C
         *******/
        ClinicalData clinicalData = buildClinicalData("C", "A");
        clinicalDataList.add(clinicalData);

        clinicalData = buildClinicalData("B", null);
        clinicalDataList.add(clinicalData);

        ClinicalData controlClinicalData = buildClinicalData("A", null);
        clinicalDataList.add(controlClinicalData);


        MetaData metaData = new MetaData();


        VisibleStateDeterminator visibleStateDeterminator = buildTestVisibleStateDeterminator();
        clinicalData = clinicalDataList.stream().
                filter(clinicalData1 -> "C".
                        equals(clinicalData1.getItem())).
                findFirst().get();
        String appliesInCrfOID =
                metaData.findFormOID(clinicalData.getCrfName(), clinicalData.getCrfVersion());

        ItemDefinition itemDefinition = definitionMap.get(clinicalData);
        itemDefinition.getDisplayRule().setAppliesInCrf(appliesInCrfOID);

        // missing control item name
        itemDefinition.getDisplayRule().setControlItemName("");
        boolean visible =
                visibleStateDeterminator.determineShown(clinicalData, metaData);
        assertTrue(visible);

        // missing displayRule
        itemDefinition.setDisplayRule(null);
        visible =
                visibleStateDeterminator.determineShown(clinicalData, metaData);
        assertTrue(visible);

        // missing itemDefinition
        definitionMap.remove(clinicalData);
        visible =
                visibleStateDeterminator.determineShown(clinicalData, metaData);
        assertTrue(visible);
    }

    private VisibleStateDeterminator buildTestVisibleStateDeterminator() {
        VisibleStateDeterminator visibleStateDeterminator = new VisibleStateDeterminator(clinicalDataList, definitionMap);
        return visibleStateDeterminator;
    }

    private ClinicalData buildClinicalData(String itemName, String controlItemName) {
        ClinicalData clinicalData = new ClinicalData();
        clinicalData.setItem(itemName);
        clinicalData.setLineNumber(0);
        ItemDefinition itemDefinition = new ItemDefinition();
        itemDefinition.setName(itemName);
        DisplayRule displayRule = new DisplayRule();
        displayRule.setControlItemName(controlItemName);
        itemDefinition.setDisplayRule(displayRule);
        definitionMap.put(clinicalData, itemDefinition);
        return clinicalData;
    }
}
