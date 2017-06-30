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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Represents configured OpenClinica environments which will be presented to the user during login.
 * OpenClinica environments can be added in application.yml inside resources folder
 *
 * Created by piotrzakrzewski on 18/04/16.
 */
@ConfigurationProperties(prefix = "OpenClinicaEnvironments")
@Configuration
public class OCEnvironmentsConfig {

    public final static String OC_ENV_ATTRIBUTE_NAME = "ocEnvironment";
    private List<OCEnvironment> ocEnvironments;

    public List<OCEnvironment> getOcEnvironments() {
        return ocEnvironments;
    }

    public OCEnvironmentsConfig(List<OCEnvironment> ocEnvironments) {
        this.ocEnvironments = ocEnvironments;
    }

    public void setOcEnvironments(List<OCEnvironment> ocEnvironments) {
        this.ocEnvironments = ocEnvironments;
    }

    public OCEnvironmentsConfig() {
    }


    public static class OCEnvironment {
        private String url;
        private String name;

        public OCEnvironment() {

        }

        public OCEnvironment(String url, String name, String version) {
            this.url = url;
            this.name = name;
            this.version = version;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        private String version;
    }
}
