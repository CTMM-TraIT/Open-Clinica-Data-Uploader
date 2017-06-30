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

package nl.thehyve.ocdu.factories;

import nl.thehyve.ocdu.models.OcUser;
import nl.thehyve.ocdu.models.UploadSession;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Created by piotrzakrzewski on 16/04/16.
 */
public class UserSubmittedDataFactory {

    public final static String COLUMNS_DELIMITER = "\t";

    private final UploadSession submission;
    private final OcUser user;

    public UserSubmittedDataFactory(OcUser user, UploadSession submission) {
        this.user = user;
        this.submission = submission;
    }

    public UploadSession getSubmission() {
        return submission;
    }

    public OcUser getUser() {
        return user;
    }

    protected static Optional<String[]> getHeaderRow(Path tabularFilePath) {
        try (Stream<String> lines = Files.lines(tabularFilePath)) {
            return lines.findFirst().map(UserSubmittedDataFactory::parseLine);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static Map<String, Integer> createColumnsIndexMap(String[] headerRow) {
        HashMap<String, Integer> result = new HashMap<>(headerRow.length);
        for (int i = 0; i < headerRow.length; i++) {
            if (result.containsKey(headerRow[i])) {
                throw new RuntimeException("Name" + headerRow[i] + " appears more than once in the header: " + headerRow.toString());
            }
            result.put(headerRow[i], i);
        }
        return result;
    }

    protected static String[] parseLine(String line) {
        return line.split(COLUMNS_DELIMITER);
    }

}
