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

package nl.thehyve.ocdu.validators.fileValidators;

import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.validators.ClinicalDataChecksRunner;
import nl.thehyve.ocdu.validators.clinicalDataChecks.*;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by piotrzakrzewski on 22/06/16.
 */
public class DataPreMappingValidator extends ClinicalDataChecksRunner {

    public DataPreMappingValidator(MetaData metadata, List<ClinicalData> clinicalData, List<StudySubjectWithEventsType> subjectWithEventsTypes) {
        super(metadata, clinicalData, subjectWithEventsTypes);
        Collection<ClinicalDataCrossCheck> checks = new ArrayList<>();
        checks.add(new SitesExistCrossCheck());
        checks.add(new SiteSubjectMatchCrossCheck());
        checks.add(new CrfExistsCrossCheck());
        checks.add(new StudyStatusAvailable());
        checks.add(new CrfCouldNotBeVerifiedCrossCheck());
        checks.add(new EventExistsCrossCheck());
        checks.add(new MultipleEventsCrossCheck());
        checks.add(new MultipleCrfCrossCheck());
        this.setChecks(checks);
    }
}
