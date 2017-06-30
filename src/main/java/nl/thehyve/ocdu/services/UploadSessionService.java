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

import nl.thehyve.ocdu.models.UploadSession;
import nl.thehyve.ocdu.repositories.UploadSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

/**
 * User submission aka UploadSession related methods.
 *
 * Created by piotrzakrzewski on 06/05/16.
 */
@Service
public class UploadSessionService {

    @Autowired
    UploadSessionRepository uploadSessionRepository;

    private static final String CURRENT_SESSION_ATTRIBUTE = "currentOcUploadSession";

    public UploadSession getCurrentUploadSession(HttpSession session) throws UploadSessionNotFoundException {
        UploadSession curUploadSession = (UploadSession) session.getAttribute(CURRENT_SESSION_ATTRIBUTE);
        if (curUploadSession == null || curUploadSession.getOwner() == null) {
            throw new UploadSessionNotFoundException();
        }
        return curUploadSession;
    }

    public void setCurrentUploadSession(HttpSession session, UploadSession ocSession) {
        session.setAttribute(CURRENT_SESSION_ATTRIBUTE, ocSession);
    }
}
