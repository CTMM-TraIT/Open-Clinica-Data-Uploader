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

package nl.thehyve.ocdu.soap.SOAPRequestFactories;

import nl.thehyve.ocdu.models.OCEntities.Study;
import nl.thehyve.ocdu.models.OcDefinitions.SiteDefinition;
import org.apache.commons.lang3.StringUtils;
import org.openclinica.ws.beans.SiteRefType;
import org.openclinica.ws.beans.StudyRefType;

/**
 * Created by piotrzakrzewski on 17/06/16.
 */
public class StudyRefFactory {
    //TODO: make sure all siteRefs and siteRefs are created with methods from this class
    public static StudyRefType createStudyRef(Study study) {
        StudyRefType studyRefType = new StudyRefType();
        studyRefType.setIdentifier(study.getIdentifier());
        return studyRefType;
    }

    public static StudyRefType createStudyRef(Study study, SiteDefinition siteDefinition) {
        StudyRefType studyRefType = new StudyRefType();
        studyRefType.setIdentifier(study.getIdentifier());
        if (siteDefinition != null && !siteDefinition.equals("")) {
            SiteRefType siteRef = createSiteRef(siteDefinition);
            studyRefType.setSiteRef(siteRef);
        }
        return studyRefType;
    }

    private static SiteRefType createSiteRef(SiteDefinition siteDefinition) {
        SiteRefType siteRef = new SiteRefType();
        siteRef.setIdentifier(siteDefinition.getSiteOID());
        return siteRef;
    }

}
