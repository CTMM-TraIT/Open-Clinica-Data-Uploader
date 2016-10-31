package nl.thehyve.ocdu.services;

import nl.thehyve.ocdu.models.OcDefinitions.MetaData;

/**
 * Created by jacob on 9/7/16.
 */
public interface MetaDataProvider {

    /**
     * Should provide the {@link MetaData} previously stored
     * @return
     */
    public MetaData provide();

    /**
     * Should provide the HTTP-session cookie for reuse.
     * @return
     */
    public String provideSessionCookie();

    /**
     * Should store the {@link MetaData} for retrieval later on
     * @param metaData
     */
    public void store(MetaData metaData);

    /**
     * Should discardMetaData the previously stored {@link MetaData}
     */
    public void discardMetaData();


    /**
     * should store the OpenClinica session ID
     * @param sessionID
     */
    public void storeOpenClinicaSessionID(String sessionID);
}
