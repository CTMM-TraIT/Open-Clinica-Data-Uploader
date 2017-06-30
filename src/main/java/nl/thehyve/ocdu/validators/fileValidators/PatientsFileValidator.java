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

package nl.thehyve.ocdu.validators.fileValidators;

import nl.thehyve.ocdu.factories.PatientDataFactory;
import nl.thehyve.ocdu.models.errors.FileFormatError;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Created by piotrzakrzewski on 11/04/16.
 */
public class PatientsFileValidator extends GenericFileValidator{

    private boolean onlyYearOfBirthUsed;

    public PatientsFileValidator(boolean onlyYearOfBirthUsed) {
        super(PatientDataFactory.MANDATORY_HEADERS, new String[]{});
        this.onlyYearOfBirthUsed = onlyYearOfBirthUsed;
    }

    @Override
    public void validateFile(Path file) {
        super.validateFile(file);
        try {
            String header = getHeader(file);
            List<String> allowed = onlyYearOfBirthUsed ?
                    Arrays.asList(PatientDataFactory.ONLY_YEAR_OF_BIRTH_ALL_PERMITTED_COLUMNS) :
                    Arrays.asList(PatientDataFactory.COMPLETE_BIRTH_DATE_ALL_PERMITTED_COLUMNS);
            noOtherColumnsAllowed(header, allowed);
        } catch (IOException e) {
            setValid(false);
            addError(new FileFormatError("Internal Server Error prevented parsing the file. Contact administrator."));
            e.printStackTrace();
        }

    }

    private void noOtherColumnsAllowed(String header, List<String> allowed) {
        List<String> headerSplit = splitLine(header);
        for (String columnName: headerSplit) {
            if (!allowed.contains(columnName)) {
                setValid(false);
                addError(new FileFormatError("Column name not allowed: " + columnName));
            }
        }
    }
}
