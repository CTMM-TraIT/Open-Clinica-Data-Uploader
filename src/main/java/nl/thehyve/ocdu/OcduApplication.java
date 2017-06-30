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

package nl.thehyve.ocdu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OcduApplication {

    public static void main(String[] args) {
        SpringApplication.run(OcduApplication.class, args);
    }

    private static final Logger log = LoggerFactory.getLogger(OcduApplication.class);
    /*@Bean
    public CommandLineRunner testData(UploadSessionRepository repository, OCUserRepository usrRepository) {
        return (args) -> {
            log.info("Loading autonomous module...");
            ApplicationContext context =
                    new ClassPathXmlApplicationContext(new String[] {"autonomous.xml"});
            log.info("Generating test data ...");
            OcUser bogusUser= new OcUser();
            bogusUser.setUsername("bogao");
            bogusUser.setOcEnvironment("http://ocdu-openclinica-dev.thehyve.net/OpenClinica-ws");
            usrRepository.save(bogusUser);
            repository.save(new UploadSession("session1", UploadSession.Step.MAPPING, new Date(),
                    bogusUser));
            repository.save(new UploadSession("session2", UploadSession.Step.EVENTS, new Date(),
                    bogusUser));
            repository.save(new UploadSession("session3", UploadSession.Step.MAPPING, new Date(),
                    bogusUser));
            repository.save(new UploadSession("session4", UploadSession.Step.SUBJECTS, new Date(),
                    bogusUser));
            repository.save(new UploadSession("session5", UploadSession.Step.OVERVIEW, new Date(),
                    bogusUser));
            repository.save(new UploadSession("session6", UploadSession.Step.MAPPING, new Date(),
                    bogusUser));

        };
    }*/

}
