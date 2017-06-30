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
import org.openclinica.ws.beans.EventType;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;

public class ScheduleEventRequestDecorator implements SoapDecorator {

    private static final String BEANS_NAMESPACE = "beans";

    private EventType eventType;

    public ScheduleEventRequestDecorator(EventType eventType) {
        this.eventType = eventType;
    }

    public void decorateBody(SOAPEnvelope envelope) throws Exception {
        SOAPBody soapBody = envelope.getBody();
        SOAPElement isStudySubjectRequest = soapBody.addChildElement("scheduleRequest", "v1");
        SOAPElement event = isStudySubjectRequest.addChildElement("event", "v1");
        SOAPElement studySubjectRefElement = event.addChildElement("studySubjectRef", BEANS_NAMESPACE );
        SOAPElement subjectLabelElement = studySubjectRefElement.addChildElement("label", BEANS_NAMESPACE );
        subjectLabelElement.setTextContent(eventType.getStudySubjectRef().getLabel());


        SOAPElement studyRefElement = event.addChildElement("studyRef", BEANS_NAMESPACE );
        SOAPElement studyIdentifierElement = studyRefElement.addChildElement("identifier", BEANS_NAMESPACE );
        studyIdentifierElement.setTextContent(eventType.getStudyRef().getIdentifier());

        if ((eventType.getStudyRef().getSiteRef() != null) &&
                (! StringUtils.isEmpty(eventType.getStudyRef().getSiteRef().getIdentifier()))) {
            SOAPElement siteRefElement = studyRefElement.addChildElement("siteRef", BEANS_NAMESPACE );
            SOAPElement siteIdentifierElement = siteRefElement.addChildElement("identifier", BEANS_NAMESPACE );
            siteIdentifierElement.setTextContent(eventType.getStudyRef().getSiteRef().getIdentifier());
        }

        SOAPElement eventDefinitionOIDElement = event.addChildElement("eventDefinitionOID", BEANS_NAMESPACE );
        eventDefinitionOIDElement.setTextContent(eventType.getEventDefinitionOID());


        SOAPElement locationElement = event.addChildElement("location", BEANS_NAMESPACE);
        locationElement.setTextContent(eventType.getLocation());

        if (eventType.getStartDate() != null) {
            SOAPElement startDateElement = event.addChildElement("startDate", BEANS_NAMESPACE);
            startDateElement.setTextContent(convertDate(eventType.getStartDate()));
        }

        if (eventType.getStartTime() != null) {
            SOAPElement startTimeElement = event.addChildElement("startTime", BEANS_NAMESPACE);
            startTimeElement.setTextContent(convertTime(eventType.getStartTime()));
        }

        if (eventType.getEndDate() != null) {
            SOAPElement endDateElement = event.addChildElement("endDate", BEANS_NAMESPACE);
            endDateElement.setTextContent(convertDate(eventType.getEndDate()));
        }

        if (eventType.getEndTime() != null) {
            SOAPElement endTimeElement = event.addChildElement("endTime", BEANS_NAMESPACE);
            endTimeElement.setTextContent(convertTime(eventType.getEndTime()));
        }
    }

    private String convertTime(XMLGregorianCalendar calendar) {
        return calendar.toString();
    }

    private String convertDate(XMLGregorianCalendar calendar) {
        return calendar.toString();
    }
}
