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
     * Should store the {@link MetaData} for retrieval later on
     * @param metaData
     */
    public void store(MetaData metaData);

    /**
     * Should discard the previously stored {@link MetaData}
     */
    public void discard();
}
