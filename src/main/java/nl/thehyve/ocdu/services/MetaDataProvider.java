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
