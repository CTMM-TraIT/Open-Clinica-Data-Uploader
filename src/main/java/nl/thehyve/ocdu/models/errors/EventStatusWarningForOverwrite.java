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
 * Warning for the user that the data-file contains data which is already present in a OpenClinica CRF and which
 * has the status <code>data entry started</code> or <code>data entry completed</code>.
 * Created by jacob on 9/13/16.
 */
public class EventStatusWarningForOverwrite extends ValidationErrorMessage {

    public EventStatusWarningForOverwrite() {
        super("The CRF for which you are about to upload data has the status 'Initial Data Entry' or 'Data Entry Completed' for one or more subjects");
    }
}
