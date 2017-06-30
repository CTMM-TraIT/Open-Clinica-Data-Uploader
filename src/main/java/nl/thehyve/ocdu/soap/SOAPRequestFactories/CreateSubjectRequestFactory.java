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
import nl.thehyve.ocdu.models.OCEntities.Subject;
import nl.thehyve.ocdu.models.OcDefinitions.SiteDefinition;
import org.apache.commons.lang3.StringUtils;
import org.openclinica.ws.beans.*;
import org.openclinica.ws.studysubject.v1.CreateRequest;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.namespace.QName;

import static nl.thehyve.ocdu.soap.SOAPRequestFactories.StudyRefFactory.createStudyRef;
import static nl.thehyve.ocdu.soap.SOAPRequestFactories.StudySubjectFactory.createStudySubject;

/**
 * Created by piotrzakrzewski on 17/06/16.
 */
public class CreateSubjectRequestFactory {
    private static QName createRequestQname = new QName("http://openclinica.org/ws/studySubject/v1", "createRequest");

    public static JAXBElement<CreateRequest> getCreateRequests(Subject subject) {
        try {
            Study study = new Study(subject.getStudyProtocolName(), subject.getStudy(), subject.getStudy());  //TODO: check if it needs to be an identifier or a name
            SiteDefinition site = new SiteDefinition();
            String siteText = subject.getSite();
            if (siteText != null && !siteText.equals("")) {
                site.setSiteOID(siteText); //TODO: We need to get an OID here and not name? Probably we need to use metadata here to get correct site
            } else {
                site = null;
            }
            return getCreateRequest(subject, study, site);
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static JAXBElement<CreateRequest> getCreateRequest(Subject subject, Study study, SiteDefinition site) throws DatatypeConfigurationException {
        CreateRequest request = new CreateRequest();
        StudySubjectType studySubject = createStudySubject(subject, study, site);
        if (StringUtils.isNotEmpty(subject.getSecondaryId())) {
            studySubject.setSecondaryLabel(subject.getSecondaryId());
        }
        request.setStudySubject(studySubject);
        JAXBElement<CreateRequest> requestWrapped = new JAXBElement<>(createRequestQname, CreateRequest.class, null, request);
        return requestWrapped;
    }

}
