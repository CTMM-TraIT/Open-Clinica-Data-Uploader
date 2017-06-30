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

package nl.thehyve.ocdu.autonomous;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

/**
 * Controller to coordinate the autonomous upload process
 *
 * Created by jacob on 5/11/16.
 */
@Component
public class AutonomousUploadTask {

    private static final Logger log = LoggerFactory.getLogger(AutonomousUploadTask.class);

    private FileCopyService fileCopyService;

    public void run() {
        log.info("Running autonomousUploadTask...");
        try {
            fileCopyService.start();
            List<Path> filePathList = fileCopyService.obtainWorkList();
            if (filePathList.size() > 0) {
                log.info("Found " + filePathList.size() + " files.");
                for (Path filePath : filePathList) {
                    // how do you obtain the associated mapping file from the input files?
                    fileCopyService.successfulFile(filePath);
                }
            }
            else {
                log.info("No files found, stopping.");
            }
            fileCopyService.stop();
        }
        catch (Exception e) {
            log.error("Failed to complete run: " + e.getMessage());
            return;
        }
        log.info("Finished running autonomousUploadTask");
    }

    public void setFileCopyService(FileCopyService fileCopyService) {
        this.fileCopyService = fileCopyService;
    }
}
