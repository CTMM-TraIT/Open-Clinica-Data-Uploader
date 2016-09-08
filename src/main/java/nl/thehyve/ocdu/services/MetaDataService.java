package nl.thehyve.ocdu.services;

import nl.thehyve.ocdu.models.OCEntities.Study;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.OcUser;
import nl.thehyve.ocdu.models.UploadSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

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


    public MetaData retrieveMetaData(MetaDataProvider metaDataProvider, OcUser user, String pwdHash, UploadSession uploadSession) throws Exception {
        // The exception is thrown on because this class is only used by other services. It's their task to
        // deal with the exception.
        MetaData metaData = null;
        String username = user.getUsername();
        Study study = dataService.findStudy(uploadSession.getStudy(), user, pwdHash);
        metaData = metaDataProvider.provide();
        if (metaData == null) {
            String url = user.getOcEnvironment();
            metaData = openClinicaService.getMetadata(username, pwdHash, url, study);
            metaDataProvider.store(metaData);
        }
        return metaData;

    }
}
