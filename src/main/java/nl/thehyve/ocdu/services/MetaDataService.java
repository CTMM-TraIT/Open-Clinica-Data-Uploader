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

import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OCEntities.Study;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.OcUser;
import nl.thehyve.ocdu.models.UploadSession;
import nl.thehyve.ocdu.repositories.ClinicalDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Retrieves the current studies {@link MetaData} using the {@link HttpSession}.
 * Created by jacob on 9/7/16.
 */
@Service
public class MetaDataService {

    @Autowired
    OpenClinicaService openClinicaService;

    @Autowired
    OcUserService ocUserService;

    @Autowired
    UploadSessionService uploadSessionService;

    @Autowired
    DataService dataService;

    @Autowired
    ClinicalDataRepository clinicalDataRepository;



    public MetaData retrieveMetaData(MetaDataProvider metaDataProvider, OcUser user, String pwdHash, UploadSession uploadSession) throws Exception {
        // The exception is thrown on because this class is only used by other services. It's their task to
        // deal with the exception.
        MetaData metaData = null;
        String username = user.getUsername();
        Study study = dataService.findStudy(uploadSession.getStudy(), user, pwdHash);
        metaData = metaDataProvider.provide();
        if (metaData == null) {
            String url = user.getOcEnvironment();
            List<ClinicalData> clinicalDataList = clinicalDataRepository.findBySubmission(uploadSession);
            Set<String> sitesPresentInData =
                    clinicalDataList.stream().map(ClinicalData::getSite).collect(Collectors.toCollection(HashSet::new));
            metaData = openClinicaService.getMetadata(username, pwdHash, url, study, sitesPresentInData);
            metaDataProvider.store(metaData);
        }
        return metaData;

    }
}
