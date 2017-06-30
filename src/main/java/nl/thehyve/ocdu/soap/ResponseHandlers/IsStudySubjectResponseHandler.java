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
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.soap.SOAPMessage;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import static nl.thehyve.ocdu.soap.ResponseHandlers.SoapUtils.toDocument;

public class IsStudySubjectResponseHandler extends OCResponseHandler {

    /**
     * Retrieve the study subjects technical ID; <code>studuSubjectOID</code> in OpenClinica
     * terminology.
     * @param response the SOAP-response
     * @return <code>null</code> if the provided subject label does not exist in the study otherwise
     * the <code>studySubjectOID</code>
     * @throws Exception on authentication failures or response structure mismatch
     */
    public static String parseIsStudySubjectResponse(SOAPMessage response) throws Exception {
        if (response == null) {
            return null;
        }
        Document document = toDocument(response);
        String result = isAuthFailure(document);
        if (! StringUtils.isEmpty(result)) {
            throw new AuthenticationCredentialsNotFoundException("Problem calling OpenClinica web-services: " + result);
        }
        XPath xpath = XPathFactory.newInstance().newXPath();
        Node createResponseNode = (Node) xpath.evaluate("//createResponse", document, XPathConstants.NODE);
        Node resultNode = (Node) xpath.evaluate("//result", createResponseNode, XPathConstants.NODE);
        if ("Success".equals(resultNode.getTextContent())) {
            Node subjectOIDNode = (Node) xpath.evaluate("//subjectOID", createResponseNode, XPathConstants.NODE);
            if (subjectOIDNode != null) {
                return subjectOIDNode.getTextContent();
            }
            throw new IllegalStateException("SubjectOID node is null");
        }
        else {
            return null;
        }
    }
}
