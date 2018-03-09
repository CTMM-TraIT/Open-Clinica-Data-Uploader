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

package nl.thehyve.ocdu.validators;

import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.validators.clinicalDataChecks.*;
import nl.thehyve.ocdu.validators.clinicalDataChecks.CRFVersionMatchCrossCheck;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.*;

/**
 * Created by piotrzakrzewski on 04/05/16.
 */
public class ClinicalDataOcChecks extends ClinicalDataChecksRunner{

    public ClinicalDataOcChecks(MetaData metadata, List<ClinicalData> clinicalData, List<StudySubjectWithEventsType> subjectWithEventsTypes) {
        super(metadata, clinicalData, subjectWithEventsTypes);
        Collection<ClinicalDataCrossCheck> crossChecks = new ArrayList<>();
        Collection<ClinicalDataCrossCheck> checksOverSubjects = new ArrayList<>();

        crossChecks.add(new EventExistsCrossCheck());
        crossChecks.add(new DataFieldWidthCrossCheck());
        crossChecks.add(new CrfExistsCrossCheck());
        crossChecks.add(new CRFVersionMatchCrossCheck());
        crossChecks.add(new CrfCouldNotBeVerifiedCrossCheck());
        crossChecks.add(new MultipleEventsCrossCheck());
        crossChecks.add(new MultipleStudiesCrossCheck());
        crossChecks.add(new ItemLengthCrossCheck());
        crossChecks.add(new ItemExistenceCrossCheck());
        crossChecks.add(new MandatoryInCrfCrossCheck());
        crossChecks.add(new DataTypeCrossCheck());
        crossChecks.add(new ValuesNumberCrossCheck());
        crossChecks.add(new RangeChecks());
        crossChecks.add(new SignificanceCrossCheck());
        checksOverSubjects.add(new SsidUniqueCrossCheck());
        crossChecks.add(new EventRepeatCrossCheck());
        crossChecks.add(new CodeListCrossCheck());
        crossChecks.add(new HiddenValueEmptyCheck());
        crossChecks.add(new HiddenTogglePresent());
        crossChecks.add(new ItemGroupRepeat());
        crossChecks.add(new DataFieldWidthCheck());
        crossChecks.add(new EventStatusCheck());
        checksOverSubjects.add(new EventGapCrossCheck());
        crossChecks.add(new EventRepeatFormatCheck());
        crossChecks.add(new EventStatusWarning());

        this.setChecks(crossChecks);
        this.setChecksOverSubjects(checksOverSubjects);
    }
}
