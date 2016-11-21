package nl.thehyve.ocdu.models.OCEntities;

import nl.thehyve.ocdu.models.OcDefinitions.ODMElement;
import nl.thehyve.ocdu.models.errors.ErrorClassification;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Meant as a simple structure to bundle together Study Name, OID and identifier.
 * Ideally this class should be merged with class Metadata - as they overlap in responsibility of representing
 * study.
 * Created by piotrzakrzewski on 15/04/16.
 */
public class Study extends AbstractStudySiteBase implements ODMElement {

    private List<Site> siteList = new ArrayList<>();

    public Study(String identifier, String oid, String name) {
        super(identifier, oid, name);
    }

    public List<Site> getSiteList() {
        return siteList;
    }

    public void addSite(Site site) {
        siteList.add(site);
    }

    public boolean hasErrorOfType(ErrorClassification errorClassification) {
        throw new UnsupportedOperationException("Cannot return error type");
    }

    public void addErrorClassification(ErrorClassification errorClassification) {
        throw new UnsupportedOperationException("Cannot add error type");
    }

    public String getSsid() {
        throw new UnsupportedOperationException("Cannot provide study subject ID on study level");
    }

    public String getStudy() {
        return name;
    }

    public String getStudyProtocolName() {
        return identifier;
    }

    @Override
    public String toString() {
        String sites = siteList.stream()
                                .map(Object::toString)
                                .collect(Collectors.joining("{",",","}"));
        return "Study{" +
                "identifier='" + identifier + '\'' +
                ", oid='" + oid + '\'' +
                ", name='" + name + '\'' +
                '}' + sites;
    }
}
