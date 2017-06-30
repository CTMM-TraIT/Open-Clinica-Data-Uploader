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
