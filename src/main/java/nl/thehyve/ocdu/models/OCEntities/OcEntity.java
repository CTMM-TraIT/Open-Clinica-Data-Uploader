package nl.thehyve.ocdu.models.OCEntities;

import nl.thehyve.ocdu.models.errors.ErrorClassification;

/**
 * Created by piotrzakrzewski on 04/05/16.
 */
public interface OcEntity {
    String getSsid();
    String getStudy();
    String getStudyProtocolName();

   /**
    * Should return true if the entity has an error of the type {@link ErrorClassification}.
    * @param errorClassification the error class
    * @return <code>true</code> if the entity has an error of the type {@link ErrorClassification}
    */
    public boolean hasErrorOfType(ErrorClassification errorClassification);

    /**
     * Should add an error of the class {@link ErrorClassification} to the entity
     * @param errorClassification
     */
    public void addErrorClassification(ErrorClassification errorClassification);
}
