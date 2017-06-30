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

import org.apache.commons.lang3.StringUtils;
import org.springframework.xml.transform.StringResult;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * SOAP related utility methods specific to this project.
 *
 * Created by piotrzakrzewski on 15/04/16.
 */
public class SoapUtils {

    public static Document toDocument(SOAPMessage soapMsg) //TODO: handle exception
            throws TransformerException, SOAPException, IOException, ParserConfigurationException, SAXException {
        final StringWriter sw = new StringWriter();
        TransformerFactory.newInstance().newTransformer().transform(
                new DOMSource(soapMsg.getSOAPPart()),
                new StreamResult(sw));
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        String xmlString = sw.toString();
        return builder.parse( new InputSource( new StringReader( xmlString ) ) );
    }

    public static Document unEscapeCDATAXML(String escapedXml) {
        //String xmlString = StringEscapeUtils.unescapeXml(escapedXml);
        return simpleString2XmlDoc(escapedXml);
    }

    public static Document simpleString2XmlDoc(String xmlString) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            return builder.parse( new InputSource( new StringReader( xmlString ) ) );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static XMLGregorianCalendar getFullXmlDate(String dateString) {
        try {
            Calendar calendar = GregorianCalendar.getInstance();
            if (! StringUtils.isEmpty(dateString)) {
                DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                Date date = dateFormat.parse(dateString);
                calendar.setTime(date);
            }
            return DatatypeFactory.newInstance().newXMLGregorianCalendarDate(calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), DatatypeConstants.FIELD_UNDEFINED);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static XMLGregorianCalendar getFullXmlTime(String timeString) {
        try {
            Calendar calendar = GregorianCalendar.getInstance();
            if (! StringUtils.isEmpty(timeString)) {
                DateFormat dateFormat = new SimpleDateFormat("HH:mm");
                Date date = dateFormat.parse(timeString);
                calendar.setTime(date);
            }
            return DatatypeFactory.newInstance().newXMLGregorianCalendarTime(calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND), DatatypeConstants.FIELD_UNDEFINED);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String soapMessageToString(SOAPMessage soapMessage) throws Exception {
        Source xmlInput = new DOMSource(soapMessage.getSOAPPart());
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        StringResult stringResult = new StringResult();
        transformer.transform(xmlInput, stringResult);
        return stringResult.toString();
    }
}
