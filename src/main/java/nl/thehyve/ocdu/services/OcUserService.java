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

import nl.thehyve.ocdu.OCEnvironmentsConfig;
import nl.thehyve.ocdu.models.OcUser;
import nl.thehyve.ocdu.repositories.OCUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by piotrzakrzewski on 06/05/16.
 */
@Service
public class OcUserService {

    private static final Logger log = LoggerFactory.getLogger(OcUserService.class);

    @Autowired
    OCUserRepository userRepository;

    public OcUser getCurrentOcUser(HttpSession session) throws UploadSessionNotFoundException { //TODO: Add tests
        String ocEnv = (String) session.getAttribute(OCEnvironmentsConfig.OC_ENV_ATTRIBUTE_NAME);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName(); //get logged in username
        List<OcUser> byUsername = userRepository.findByUsername(username);
        List<OcUser> matching = byUsername.stream().filter(usr -> usr.getOcEnvironment().equals(ocEnv))
                .collect(Collectors.toList());
        if (matching.size() == 1) {
            return matching.get(0);
        } else if (matching.size() > 1) {
            log.error("More than one matching users sharing the same username and OcEnv: " + matching.toString());
            return null;
        } else {
            throw new UploadSessionNotFoundException();
        }
    }

    public String getOcwsHash(HttpSession session) {
        String ocwsHash = (String) session.getAttribute("ocwsHash");
        return ocwsHash;
    }
}
