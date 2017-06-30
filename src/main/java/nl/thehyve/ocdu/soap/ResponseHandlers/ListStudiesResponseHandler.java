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

import nl.thehyve.ocdu.models.OCEntities.AbstractStudySiteBase;
import nl.thehyve.ocdu.models.OCEntities.Site;
import nl.thehyve.ocdu.models.OCEntities.Study;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.soap.SOAPMessage;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.List;

import static nl.thehyve.ocdu.soap.ResponseHandlers.SoapUtils.toDocument;

/**
 * Responsible for deserializing SOAP response for ListStudies call.
 *
 * Created by piotrzakrzewski on 15/04/16.
 */
public class ListStudiesResponseHandler extends OCResponseHandler {

    private enum StudySiteType {
        STUDY, SITE
    }

    public static List<Study> parseListStudiesResponse(SOAPMessage response) throws Exception { //TODO: handle exception
        Document document = toDocument(response);
        String result = isAuthFailure(document);
        if (! StringUtils.isEmpty(result)) {
            throw new AuthenticationCredentialsNotFoundException("Problem calling OpenClinica web-services: " + result);
        }
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList studyNodes = (NodeList) xpath.evaluate("//listAllResponse/studies/study", document, XPathConstants.NODESET);
        List<Study> studiesParsed = new ArrayList<>();
        for (int i = 0; i < studyNodes.getLength(); i++) {
            Node studyNode = studyNodes.item(i);
            Study study = parseStudy(studyNode);
            studiesParsed.add(study);
        }
        return studiesParsed;
    }

    public static Study parseStudy(Node studyElement) throws Exception { //TODO: handle exception
        Study ret = (Study) createStudySite(studyElement, StudySiteType.STUDY);
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList siteNodes = (NodeList) xpath.evaluate(".//sites/site", studyElement, XPathConstants.NODESET);
        for (int i = 0; i < siteNodes.getLength(); i++) {
            Node siteNode = siteNodes.item(i);
            AbstractStudySiteBase site = createStudySite(siteNode, StudySiteType.SITE);
            ret.addSite((Site) site);
        }
        return ret;
    }

    public static AbstractStudySiteBase createStudySite(Node element, StudySiteType typeToCreate) throws Exception {
        XPath xpath = XPathFactory.newInstance().newXPath();
        Node identifier = (Node) xpath.evaluate("./identifier", element, XPathConstants.NODE);
        Node oid = (Node) xpath.evaluate("./oid", element, XPathConstants.NODE);
        Node name = (Node) xpath.evaluate("./name", element, XPathConstants.NODE);
        AbstractStudySiteBase ret;
        if (typeToCreate == StudySiteType.STUDY) {
            ret = new Study(identifier.getTextContent(), oid.getTextContent(), name.getTextContent());
        }
        else {
            ret = new Site(identifier.getTextContent(), oid.getTextContent(), name.getTextContent());
        }
        return ret;
    }


}
