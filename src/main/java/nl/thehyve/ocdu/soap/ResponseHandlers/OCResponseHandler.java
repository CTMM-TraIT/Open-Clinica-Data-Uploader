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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.soap.SOAPMessage;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import static nl.thehyve.ocdu.soap.ResponseHandlers.SoapUtils.toDocument;

/**
 * Created by piotrzakrzewski on 18/04/16.
 */
public class OCResponseHandler {

    private static final Logger log = LoggerFactory.getLogger(OCResponseHandler.class);


    public final static String authFailXpathExpr =  "//faultstring";


    /**
     * Checks if an error occurred in the call to OpenCLinica. Returns a empty String if no error occurred else it
     * returns the OpenClinica message.
     * @param xmlResponse
     * @return
     */
    public static String isAuthFailure(Document xmlResponse) {
        XPath xpath = XPathFactory.newInstance().newXPath();
        Node faultNode;
        try {
            faultNode = (Node) xpath.evaluate(authFailXpathExpr,  //TODO: make it more specific, can we distinguish between different faultcodes?
                    xmlResponse, XPathConstants.NODE);
            if (faultNode == null) {
                return "";
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return e.getMessage(); // Do not proceed when auth status cannot be resolved.
        }
        return faultNode.getTextContent();
    }

    public static String parseGenericResponse(SOAPMessage response, String selector) throws Exception {
        Document document = toDocument(response);
        log.debug("SOAP -->\n" + SoapUtils.soapMessageToString(response));
        if (! isAuthFailure(document).equals("")) {
            throw new AuthenticationCredentialsNotFoundException("Authentication against OpenClinica unsuccessfull");
        }
        XPath xpath = XPathFactory.newInstance().newXPath();
        Node importDataResponseNode = (Node) xpath.evaluate(selector, document, XPathConstants.NODE);
        Node resultNode = (Node) xpath.evaluate("//result", importDataResponseNode, XPathConstants.NODE);
        if ("fail".equalsIgnoreCase(resultNode.getTextContent())) {
            Node errorNode = (Node) xpath.evaluate("//error", importDataResponseNode, XPathConstants.NODE);
            return errorNode.getTextContent();
        }
        return null;
    }

}
