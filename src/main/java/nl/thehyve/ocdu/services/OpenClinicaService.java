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

package nl.thehyve.ocdu.services;

import nl.thehyve.ocdu.models.OCEntities.*;
import nl.thehyve.ocdu.models.OcDefinitions.EventDefinition;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.OcDefinitions.RegisteredEventInformation;
import nl.thehyve.ocdu.models.OcDefinitions.SiteDefinition;
import nl.thehyve.ocdu.models.UploadSession;
import nl.thehyve.ocdu.models.errors.AbstractMessage;
import nl.thehyve.ocdu.models.errors.ODMUploadErrorMessage;
import nl.thehyve.ocdu.models.errors.SubmissionResult;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.soap.ResponseHandlers.*;
import nl.thehyve.ocdu.soap.SOAPRequestFactory;
import org.apache.commons.lang3.StringUtils;
import org.openclinica.ws.beans.EventResponseType;
import org.openclinica.ws.beans.EventType;
import org.openclinica.ws.beans.StudySubjectRefType;
import org.openclinica.ws.beans.StudySubjectWithEventsType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.ws.soap.SoapMessage;
import org.w3c.dom.Document;

import javax.servlet.http.HttpSession;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPMessage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.*;
import java.util.stream.Collectors;

import static nl.thehyve.ocdu.soap.ResponseHandlers.RegisterSubjectsResponseHandler.parseRegisterSubjectsResponse;

/**
 * Wrapper for OpenClinica webservices - or at least the calls that are relevant to to validation, registering subjects,
 * events and uploading data. This is the only place in the application where new WS calls to OpenClnica should be
 * implemented.
 *
 * Created by piotrzakrzewski on 11/04/16.
 */

@Service
public class OpenClinicaService {
    /**
     * Connection time out (15 seconds). Value set in the application.properties file
     */
    @Value("${soap.connection.timeout.in.milliseconds}")
    private Integer CONNECTION_TIMEOUT;

    /**
     * The read-time out = 30 minutes
     */
    @Value("${soap.read.timeout.in.milliseconds}")
    private Integer READ_TIMEOUT;


    @Autowired
    ODMService odmService;

    SOAPRequestFactory requestFactory = new SOAPRequestFactory();

    private static final Logger log = LoggerFactory.getLogger(OpenClinicaService.class);


    public AbstractMessage registerPatient(String username, String passwordHash, String url, Subject subject)
            throws Exception {
        AbstractMessage returnMessage;
        SOAPMessage soapMessage = requestFactory.createCreateSubject(username, passwordHash, subject);
//        log.info(SoapUtils.soapMessageToString(soapMessage));
        SOAPMessage soapResponse = safeCallSoap(soapMessage, url + "/ws/studySubject/v1");

        String error = parseRegisterSubjectsResponse(soapResponse);
        if (error != null) {
            String detailedErrorMessage = "Creating subject " + subject.getSsid() + " against instance " + url + " failed, OC error: " + error;
            log.error(detailedErrorMessage);
            returnMessage = new ValidationErrorMessage(detailedErrorMessage);
        }
        else {
            String detailedMessage = "Subject " + subject.getSsid() + " successfully registered";
            returnMessage = new SubmissionResult(detailedMessage);
            log.info("Subject: '" + subject.getSsid() + "' created in instance " + url + ".");
        }
        returnMessage.setSubject(subject.getSsid());
        return returnMessage;
    }

    public List<Study> listStudies(String username, String passwordHash, String url) throws Exception { //TODO: handle exceptions
        log.info("List studies initiated by: " + username + " on: " + url);
        SOAPMessage message = requestFactory.createListStudiesRequest(username, passwordHash);
        SOAPMessage soapResponse = safeCallSoap(message, url + "/ws/study/v1");
        return ListStudiesResponseHandler.parseListStudiesResponse(soapResponse);
    }

    public Map<String, String> createMapSubjectLabelToSubjectOID(String username,
                                                                 String passwordHash,
                                                                 String url,
                                                                 List<ClinicalData> clinicalDataList) throws Exception {
        // TODO this mapping can be made redundant if the subjectOID is also returned by the listAllByStudy
        // call. In this way you avoid N-calls toe isStudySubject for N-subjects.
        // We assume that all subject in the clinicalData-list are registered.
        Map<String, String> ret = new HashMap<>(clinicalDataList.size());
        for (ClinicalData clinicalData : clinicalDataList) {
            String subjectLabel = clinicalData.getSsid();
            if (!ret.containsKey(subjectLabel)) {
                String studyLabel = clinicalData.getStudy();
                String siteLabel = clinicalData.getSite();
                String subjectOID = getSubjectOID(username, passwordHash, url, studyLabel, siteLabel, subjectLabel);
                ret.put(subjectLabel, subjectOID);
            }
        }
        return ret;
    }


    public MetaData getMetadata(String username, String passwordHash, String url, Study study, Set<String> sitesPresentInData) throws Exception {
        log.info("Get metadata initiated by: " + username + " on: " + url + " study: " + study);
        if (study == null || username == null || passwordHash == null || url == null) {
            return null;
        }
        MetaData metaData = getMetadataSoapCall(username, passwordHash, url, study);
        addSiteDefinitions(metaData, username, passwordHash, url, study, sitesPresentInData);
        addSiteInformationToMetaData(metaData, study);
        return metaData;
    }

    private MetaData getMetadataSoapCall(String username, String passwordHash, String url, Study study) throws Exception {
        SOAPMessage message = requestFactory.createGetStudyMetadataRequest(username, passwordHash, study);
        SOAPMessage soapResponse  = safeCallSoap(message, url + "/ws/study/v1");
        return GetStudyMetadataResponseHandler.parseGetStudyMetadataResponse(soapResponse);
    }


    /**
     * Performs the actual upload to OpenClinica of the data in the clinicalDataList.
     * @param username the user performing the upload
     * @param passwordHash the user's hashed (SHA1) password
     * @param url the OpenClinica URL
     * @param clinicalDataList the list with clinical data
     * @param metaData the meta data of the study
     * @param uploadSession the upload session with the relevenat information
     * @return a list of messages with errors and/or results
     * @throws Exception in case of an error
     */
    public Collection<AbstractMessage> uploadODM(String username,
                                                                 String passwordHash,
                                                                 String url,
                                                                 List<ClinicalData> clinicalDataList,
                                                                 MetaData metaData,
                                                                 UploadSession uploadSession) throws Exception {
        log.info("Upload initiated by: " + username + " on: " + url);
        List<AbstractMessage> resultList = new ArrayList<>();

        if (StringUtils.isEmpty(username) ||
                StringUtils.isEmpty(passwordHash) ||
                StringUtils.isEmpty(url)) {
            resultList.add(new ODMUploadErrorMessage("One of the required parameters is missing (username, password or URL)"));
            return resultList;
        }

        Map<String, String> subjectLabelToOIDMap =
                createMapSubjectLabelToSubjectOID(username, passwordHash, url, clinicalDataList);

        Map<String, List<ClinicalData>> outputMap = clinicalDataList.stream().collect(Collectors.groupingBy(ClinicalData::createODMGroupingKey,
                Collectors.toList()));
        TreeMap<String, List<ClinicalData>> sortedMap = new TreeMap<>(outputMap);
        for (String key : sortedMap.keySet()) {
            List<ClinicalData> outputClinicalData = sortedMap.get(key);
            String odmString = odmService.generateODM(outputClinicalData, metaData, uploadSession, subjectLabelToOIDMap);
            String uploadResult = uploadODMString(username, passwordHash, url, odmString);
            if (uploadResult != null) {
                String detailedMessage = "Failed upload for subject " + key + ". Cause: " + uploadResult;
                log.error(detailedMessage);
                resultList.add(new ODMUploadErrorMessage(detailedMessage));
                break;
            }
            else {
                String detailedMessage = "Data successfully uploaded for subject " + key;
                SubmissionResult submissionResult = new SubmissionResult(detailedMessage);
                submissionResult.setSubject(key);
                resultList.add(submissionResult);
            }
        }
        return resultList;
    }

    private void addSiteDefinitions(MetaData metaData, String username, String passwordHash, String url, Study study,
                                    Set<String> sitesPresentInData) throws Exception {
        List<SiteDefinition> siteDefinitions = new ArrayList<>();
        for (Site site : study.getSiteList()) {
            String siteID = site.getIdentifier();
            if (sitesPresentInData.contains(siteID)) {
                Study siteAsAStudy = new Study(site.getIdentifier(), site.getOid(), site.getName());
                MetaData siteMetadata = getMetadataSoapCall(username, passwordHash, url, siteAsAStudy);
                SiteDefinition siteDef = new SiteDefinition();
                siteDef.setSiteOID(site.getOid());
                siteDef.setName(site.getName());
                siteDef.setUniqueID(site.getIdentifier());
                siteDef.setBirthdateRequired(siteMetadata.getBirthdateRequired());
                siteDef.setGenderRequired(siteMetadata.isGenderRequired());
                siteDefinitions.add(siteDef);
            }
        }
        metaData.setSiteDefinitions(siteDefinitions);
    }

    /**
     * @param username     the user-account name
     * @param passwordHash the SHA1 hash of the user's password
     * @param url          the URL of the OpenClinica-ws instance
     * @param odm          the ODM string to upload
     * @return a non <code>null</code> error code.message if an error occurred. Some are reported by the OpenClinica-WS
     * instance at url. Returns <code>null</code> if everything went OK.
     * @throws Exception in case of a technical error
     */
    private String uploadODMString(String username, String passwordHash, String url, String odm) throws Exception {
        SOAPMessage soapMessage = requestFactory.createDataUploadRequest(username, passwordHash, odm);
        log.debug("SOAP -->\n" + SoapUtils.soapMessageToString(soapMessage));
        SOAPMessage soapResponse = safeCallSoap(soapMessage, url + "/ws/data/v1");
        String responseError = SOAPResponseHandler.parseOpenClinicaResponse(soapResponse, "//importDataResponse");
        if (responseError != null) {
            log.error("ImportData request failed: " + responseError);
        }
        return responseError;
    }

    /**
     * Schedule all the events found in {@param eventList} but which have not been scheduled yet in
     * OpenClinica according to the information present in the {@param studyEventDefinitionTypeList}.
     *
     * @param username the user name performing the scheduling of events
     * @param passwordHash the user's password hash
     * @param url the OpenClinica URL in which the events are scheduled
     * @param eventList the event list
     * @throws Exception in case of errors
     */
    public Collection<AbstractMessage> scheduleEvents(String username, String passwordHash, String url,
                                                             MetaData metaData,
                                                             List<Event> eventList,
                                                             List<StudySubjectWithEventsType> studySubjectWithEventsTypeList) throws Exception {
        Collection<AbstractMessage> ret = new ArrayList<>();
        log.info("Schedule events initiated by: " + username + " on: " + url);
        if (StringUtils.isEmpty(username) ||
                StringUtils.isEmpty(passwordHash) ||
                StringUtils.isEmpty(url)) {
            ret.add(new ValidationErrorMessage("One of the required parameters is missing (username, password, url)"));
            return ret;
        }
        Map<String, String> eventNameOIDMap =
                metaData.getEventDefinitions().stream().collect(Collectors.toMap(EventDefinition::getName, EventDefinition::getStudyEventOID));

        Map<String, String> eventOIDToNameMap =
                metaData.getEventDefinitions().stream().collect(Collectors.toMap(EventDefinition::getStudyEventOID, EventDefinition::getName));

        Map<String, EventResponseType> eventsRegisteredInOpenClinica =
                RegisteredEventInformation.createEventKeyMap(studySubjectWithEventsTypeList);
        List<EventType> eventTypeList = new ArrayList<>();
        for (Event event : eventList) {
            String eventOID = eventNameOIDMap.get(event.getEventName());
            String eventKey = event.createEventKey(eventOID);
            if (!eventsRegisteredInOpenClinica.containsKey(eventKey)) {
                EventType eventType = event.createEventType(eventNameOIDMap);
                StudySubjectRefType studySubjectRefType = new StudySubjectRefType();
                studySubjectRefType.setLabel(event.getSsid());
                eventType.setStudySubjectRef(studySubjectRefType);

                if (StringUtils.isEmpty(eventOID)) {
                    throw new IllegalStateException("No eventName specified in the input for subject " + event.getSsid());
                }
                eventType.setEventDefinitionOID(eventOID);
                eventType.setLocation(event.getLocation());
                XMLGregorianCalendar startDate = SoapUtils.getFullXmlDate(event.getStartDate());
                eventType.setStartDate(startDate);

                if (StringUtils.isNotBlank(event.getEndDate())) {
                    XMLGregorianCalendar endDate = SoapUtils.getFullXmlDate(event.getEndDate());
                    eventType.setEndDate(endDate);
                }

                if (StringUtils.isNotBlank(event.getStartTime())) {
                    XMLGregorianCalendar startTime = SoapUtils.getFullXmlTime(event.getStartTime());
                    eventType.setStartTime(startTime);
                }

                if (StringUtils.isNotBlank(event.getEndTime())) {
                    XMLGregorianCalendar endTime = SoapUtils.getFullXmlTime(event.getEndTime());
                    eventType.setEndTime(endTime);
                }


                eventTypeList.add(eventType);
            }
        }

        for (EventType eventType : eventTypeList) {
            SOAPMessage soapMessage = requestFactory.createScheduleEventRequest(username, passwordHash, eventType);
            SOAPMessage soapResponse = safeCallSoap(soapMessage, url + "/ws/event/v1");
            String responseError = SOAPResponseHandler.parseOpenClinicaResponse(soapResponse, "//scheduleResponse");
            String eventName = eventOIDToNameMap.get(eventType.getEventDefinitionOID());
            if (responseError != null) {
                String detailedMessage = "ScheduleEvent request " + eventName + " failed for subject : "  + eventType.getStudySubjectRef().getLabel() + ". Cause: " + responseError;
                log.error(detailedMessage);
                ValidationErrorMessage validationErrorMessage = new ValidationErrorMessage(detailedMessage);
                validationErrorMessage.setSubject(eventType.getStudySubjectRef().getLabel());
                ret.add(validationErrorMessage);
                break;
            }
            else {
                String eventOrdinal = SOAPResponseHandler.returnEventOrdinal(soapResponse);
                String detailedMessage = "Successfully scheduled event '" + eventName + "', event ordinal: " + eventOrdinal+ " for subject " + eventType.getStudySubjectRef().getLabel();
                SubmissionResult submissionResult = new SubmissionResult(detailedMessage);
                submissionResult.setSubject(eventType.getStudySubjectRef().getLabel());
                ret.add(submissionResult);
            }
        }
        return ret;
    }


    public List<StudySubjectWithEventsType> getStudySubjectsType(String username, String passwordHash, String url, String studyIdentifier, String siteIdentifier) throws Exception {
        log.info("Get listAllByStudy by: " + username + " on: " + url + " study: " + studyIdentifier + " site: " + siteIdentifier);
        if (studyIdentifier == null || username == null || passwordHash == null || url == null) {
            return null;
        }
        SOAPMessage soapMessage = requestFactory.createListAllByStudy(username, passwordHash, studyIdentifier, siteIdentifier);
        SOAPMessage soapResponse = safeCallSoap(soapMessage, url + "/ws/studySubject/v1");
        return ListAllByStudyResponseHandler.retrieveStudySubjectsType(soapResponse);
    }


    private URL createEndPoint(String url) throws MalformedURLException {
        URL endPoint = new URL (null, url, new URLStreamHandler() {
            protected URLConnection openConnection (URL url) throws IOException {
                // The url is the parent of this stream handler, so must create clone
                URL clone = new URL (url.toString ());
                URLConnection connection = clone.openConnection ();
                connection.setConnectTimeout (CONNECTION_TIMEOUT);
                connection.setReadTimeout (READ_TIMEOUT);
                return connection;
            }});
        return endPoint;
    }

    private SOAPMessage safeCallSoap(SOAPMessage requestMessage, String URL) throws Exception {
        SOAPConnection soapConnection = null;
        SOAPMessage soapResponse = null;
        try {
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            soapConnection = soapConnectionFactory.createConnection();
            soapResponse = soapConnection.call(requestMessage, createEndPoint(URL));
        }
        catch (Exception e) {
            e.printStackTrace();
            // rethrow e to avoid breaking existing behaviour
            throw e;
        }
        finally {
            if (soapConnection != null) {
                soapConnection.close();
            }
        }
        return soapResponse;
    }


    public boolean isAuthenticated(String username, String /* hexdigest of sha1 password */ passwordHash, String url) throws Exception {
        SOAPMessage message = requestFactory.createListStudiesRequest(username, passwordHash);
        SOAPMessage soapResponse = safeCallSoap(message, url + "/ws/study/v1");
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attr.getRequest().getSession(false);
        MetaDataProvider provider = new HttpSessionMetaDataProvider(session);
        if (StringUtils.isEmpty(provider.provideSessionCookie())) {
            String[] ocSessionID = soapResponse.getMimeHeaders().getHeader("Set-Cookie");
            String jSessionID = StringUtils.substringBetween(ocSessionID[0], "JSESSIONID=", ";");
            provider.storeOpenClinicaSessionID(jSessionID);
        }
        Document responseXml = SoapUtils.toDocument(soapResponse);

        return StringUtils.isEmpty(OCResponseHandler.isAuthFailure(responseXml));
    }

    /**
     * Retrieves the corresponding OpenClinica studySubjectOID of a <code>subjectLabel</code>with a SOAP-call to the
     * OpenClinica instance at <code>url</code>.
     *
     * @param username     the user name
     * @param passwordHash the SHA1 hashed password
     * @param url          the url to the OpenClinica-WS instance
     * @param studyLabel    the study's name
     * @param siteLabel     the optional site name
     * @param subjectLabel  the subject's ID
     * @return <code>null</code> if the subjectLabel does not exist in the study.
     * @throws Exception in case of problems
     */
    public String getSubjectOID(String username, String passwordHash, String url, String studyLabel, String siteLabel, String subjectLabel) throws Exception {
        log.info("Get isStudySubject initiated by: " + username + " on: " + url + " study: " + studyLabel + " site: " + siteLabel);
        if (studyLabel == null || username == null || passwordHash == null || url == null || subjectLabel == null) {
            return null;
        }

        SOAPMessage message =
                requestFactory.createIsStudySubjectRequest(username, passwordHash, studyLabel, siteLabel, subjectLabel);
        SOAPMessage soapResponse = safeCallSoap(message, url + "/ws/studySubject/v1");
        return IsStudySubjectResponseHandler.parseIsStudySubjectResponse(soapResponse);
    }

    private void addSiteInformationToMetaData(MetaData metaData, Study study) {
        // TODO change outer loop to Lambda
        for (SiteDefinition siteDefinition : metaData.getSiteDefinitions()) {
            String searchOID = siteDefinition.getSiteOID();
            List<Site> searchSiteList =
                    study.getSiteList().stream().filter(site -> site.getOid().equals(searchOID)).collect(Collectors.toList());
            if (!searchSiteList.isEmpty()) {
                Site searchSite = searchSiteList.get(0);
                siteDefinition.setUniqueID(searchSite.getIdentifier());
            }
        }
    }
}
