package nl.thehyve.ocdu.services;

import nl.thehyve.ocdu.models.OcDefinitions.MetaData;

import javax.servlet.http.HttpSession;

/**
 * Created by jacob on 9/7/16.
 */
public class HttpSessionMetaDataProvider implements MetaDataProvider {

    private HttpSession session;

    private static final String METADATA_SESSION_KEY = "STORED_METADATA";

    private static final String OPEN_CLINICA_SESSION_ID_COOKIE = "OPEN_CLINICA_SESSION_ID_COOKIE";

    public HttpSessionMetaDataProvider(HttpSession session) {
        this.session = session;
    }

    public MetaData provide() {
        MetaData ret = (MetaData) session.getAttribute(METADATA_SESSION_KEY);
        return ret;
    }

    public String provideSessionCookie() {
        return (String) session.getAttribute(OPEN_CLINICA_SESSION_ID_COOKIE);
    }

    public void discardMetaData() {
        session.removeAttribute(METADATA_SESSION_KEY);
    }

    public void store(MetaData metaData) {
        session.setAttribute(METADATA_SESSION_KEY, metaData);
    }

    public void storeOpenClinicaSessionID(String sessionID ) {
        session.setAttribute(OPEN_CLINICA_SESSION_ID_COOKIE, sessionID);
    }
}
