package nl.thehyve.ocdu.services;

import nl.thehyve.ocdu.models.OcDefinitions.MetaData;

import javax.servlet.http.HttpSession;

/**
 * Created by jacob on 9/7/16.
 */
public class HttpSessionMetaDataProvider implements MetaDataProvider {

    private HttpSession session;

    private static final String METADATA_SESSION_KEY = "STORED_METADATA";

    public HttpSessionMetaDataProvider(HttpSession session) {
        this.session = session;
    }

    public MetaData provide() {
        MetaData ret = (MetaData) session.getAttribute(METADATA_SESSION_KEY);
        return ret;
    }

    public void discard() {
        session.removeAttribute(METADATA_SESSION_KEY);
    }

    public void store(MetaData metaData) {
        session.setAttribute(METADATA_SESSION_KEY, metaData);
    }
}
