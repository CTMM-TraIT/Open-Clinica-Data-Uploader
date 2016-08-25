package nl.thehyve.ocdu.soap.SOAPRequestDecorators;

import org.apache.commons.lang3.StringUtils;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;

/**
 * Decorator for the <code>isStudySubject</code> OpenClinica WS-call. This call is needed to
 * retrieve the technical subject ID <code>SubjectOID</code>. This technical ID is used in all
 * SOAP-calls to identify the subject
 * Created by Jacob Rousseau on 15-Jun-2016.
 * Copyright CTMM-TraIT / NKI (c) 2016
 */
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
