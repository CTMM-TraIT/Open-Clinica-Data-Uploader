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
 * Error to indicate that an data is present in the data file for an event which is not present in OpenClinica nor
 * present in the event data.
 * Created by jacob on 8/10/16.
 */
public class MissingEventError extends ValidationErrorMessage {

    public MissingEventError() {
        super("Event in the event scheduling file does not match the event in the data file. Please click on back, " +
                "correct the event file and upload again. If you do proceed, " +
                "the data for unscheduled events will not be uploaded");
    }
}
