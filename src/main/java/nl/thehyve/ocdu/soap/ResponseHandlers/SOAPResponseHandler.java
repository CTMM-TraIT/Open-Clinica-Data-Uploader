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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.soap.SOAPMessage;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import static nl.thehyve.ocdu.soap.ResponseHandlers.SoapUtils.toDocument;

public class SOAPResponseHandler extends OCResponseHandler {

    private static final Logger log = LoggerFactory.getLogger(SOAPResponseHandler.class);

    /**
     * Checks if an error occurred on the OpenClinica-side and reports it back as the
     * return value
     *
     * @param response the SOAP-response.
     * @return a non <code>null</code> error code.message if an error occurred. Some are reported by the OpenClinica-WS
     * instance at url. Returns <code>null</code> if everything went OK.
     * @throws Exception if a technical error occurs.
     */

    public static String parseOpenClinicaResponse(SOAPMessage response, String xPathToResponse) throws Exception {
        Document document = toDocument(response);
        log.debug("SOAP -->\n" + SoapUtils.soapMessageToString(response));
        String result = isAuthFailure(document);
        if (! StringUtils.isEmpty(result)) {
            throw new AuthenticationCredentialsNotFoundException("Problem calling OpenClinica web-services: " + result);
        }
        XPath xpath = XPathFactory.newInstance().newXPath();
        Node importDataResponseNode = (Node) xpath.evaluate(xPathToResponse, document, XPathConstants.NODE);
        Node resultNode = (Node) xpath.evaluate("//result", importDataResponseNode, XPathConstants.NODE);
        if ("fail".equalsIgnoreCase(resultNode.getTextContent())) {
            Node errorNode = (Node) xpath.evaluate("//error", importDataResponseNode, XPathConstants.NODE);
            return errorNode.getTextContent();
        }
        return null;
    }

    public static String returnEventOrdinal(SOAPMessage response) throws Exception {
        Document document = toDocument(response);
        XPath xpath = XPathFactory.newInstance().newXPath();
        Node eventOrdinalResponseNode = (Node) xpath.evaluate("//studyEventOrdinal", document, XPathConstants.NODE);
        if (eventOrdinalResponseNode != null) {
            return eventOrdinalResponseNode.getTextContent();
        }
        return "";
    }
}
