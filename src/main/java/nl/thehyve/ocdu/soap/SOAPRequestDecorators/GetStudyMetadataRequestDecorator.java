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

import nl.thehyve.ocdu.models.OCEntities.Study;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;

/**
 * Created by piotrzakrzewski on 26/04/16.
 */
public class GetStudyMetadataRequestDecorator implements SoapDecorator {

    private Study study;

    public void setStudy(Study study) {
        this.study = study;
    }

    @Override
    public void decorateBody(SOAPEnvelope envelope) throws Exception {
        SOAPBody soapBody = envelope.getBody();
        SOAPElement getMetaDataRequest = soapBody.addChildElement("getMetadataRequest", "v1");
        SOAPElement studyMetadata = getMetaDataRequest.addChildElement("studyMetadata", "v1");
        SOAPElement identifier = studyMetadata.addChildElement("identifier", "beans");
        if (study == null) {
            throw new Exception("Cannot decorateBody of getStudyMetdata request without Study. Set study first.");
        }
        identifier.setTextContent(study.getIdentifier());
    }
}
