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

package nl.thehyve.ocdu.models.errors;

/**
 * Defines the types of errors which the data contain. Is used to determine if the data should be disregarded from
 * uploads.
 * Created by jacob on 04/11/16.
 */
public enum ErrorClassification {

    /**
     * Errors in the structure of the input data and the reference to the data target
     * (e.g. missing cell in a CRF-record, missing required column, empty or missing clinical-axis cells. The
     * consequence of this error that the entire upload session is blocked
     */
    BLOCK_ENTIRE_UPLOAD,

    /**
     * Errors in the state of data in OpenClinica and in the data files (e.g. data supplied for a subject which is not
     * present in OpenClinica nor in the subject-input file). The upload of the entire CRF (row in the input file) is
     * blocked.
     */
    BLOCK_ENTIRE_CRF,

    /**
     * A problem concerning a single subject. All relevant data (subject, data and event) will be blocked from the
     * upload
     */
    BLOCK_SUBJECT,

    /**
     * A problem concerning an event. All relevant data - subject, data and the event itself - will be blocked from the
     * upload
     */
    BLOCK_EVENT,

    /**
     * An error only concerning a single item (e.g. out of range of a number)
     */
    BLOCK_SINGLE_ITEM
}
