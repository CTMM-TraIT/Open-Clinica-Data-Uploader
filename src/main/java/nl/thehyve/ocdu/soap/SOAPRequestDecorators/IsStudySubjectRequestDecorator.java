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

package nl.thehyve.ocdu.soap.SOAPRequestDecorators;

import org.apache.commons.lang3.StringUtils;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;

public class IsStudySubjectRequestDecorator implements SoapDecorator {

    private String subjectLabel;

    private String studyName;

    private String siteName;

    public IsStudySubjectRequestDecorator(String subjectLabel, String studyName, String siteName) {
        this.subjectLabel = subjectLabel;
        this.studyName = studyName;
        this.siteName = siteName;
    }


    public void decorateBody(SOAPEnvelope envelope) throws Exception {
        SOAPBody soapBody = envelope.getBody();
        SOAPElement isStudySubjectRequest = soapBody.addChildElement("isStudySubjectRequest", "v1");
        SOAPElement studySubjectElement = isStudySubjectRequest.addChildElement("studySubject", "v1");

        SOAPElement label = studySubjectElement.addChildElement("label", "beans");
        label.setTextContent(subjectLabel);
        studySubjectElement.addChildElement("subject", "beans");
        SOAPElement studyRef = studySubjectElement.addChildElement("studyRef", "beans");
        SOAPElement identifier = studyRef.addChildElement("identifier", "beans");
        if (!StringUtils.isEmpty(siteName)) {
            SOAPElement siteRef = studyRef.addChildElement("siteRef", "beans");
            SOAPElement siteIdentifier = siteRef.addChildElement("identifier", "beans");
            siteIdentifier.setTextContent(siteName);
        }
        identifier.setTextContent(studyName);
    }
}
