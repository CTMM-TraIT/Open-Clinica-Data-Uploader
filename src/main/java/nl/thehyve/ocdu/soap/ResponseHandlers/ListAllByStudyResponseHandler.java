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

package nl.thehyve.ocdu.soap.ResponseHandlers;

import org.openclinica.ws.beans.StudySubjectWithEventsType;
import org.openclinica.ws.studysubject.v1.CreateResponse;
import org.openclinica.ws.studysubject.v1.ListAllByStudyResponse;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.SOAPMessage;
import java.util.List;

/**
 * SOAP response handler to handle the listAllByStudy operation of StudySubject-webservice.
 * Created by jacob on 6/2/16.
 */
public class ListAllByStudyResponseHandler {

    public static List<StudySubjectWithEventsType> retrieveStudySubjectsType(SOAPMessage soapMessage) throws Exception {
        JAXBContext jaxbContext  = JAXBContext.newInstance(ListAllByStudyResponse.class, CreateResponse.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        ListAllByStudyResponse ret = (ListAllByStudyResponse) unmarshaller.unmarshal(soapMessage.getSOAPBody().extractContentAsDocument());
        return ret.getStudySubjects().getStudySubject();
    }

}
