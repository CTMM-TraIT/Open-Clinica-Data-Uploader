package nl.thehyve.ocdu.models.OCEntities;

import nl.thehyve.ocdu.models.OcDefinitions.ODMElement;
import nl.thehyve.ocdu.models.errors.ErrorClassification;

/**
 * Created by Jacob Rousseau on 24-Jun-2016.
 * Copyright CTMM-TraIT / NKI (c) 2016
 */
public class Site extends AbstractStudySiteBase implements ODMElement, OcEntity {

    public Site(String identifier, String oid, String name) {
        super(identifier, oid, name);
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
        return "Site{" +
                "identifier='" + identifier + '\'' +
                ", oid='" + oid + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
