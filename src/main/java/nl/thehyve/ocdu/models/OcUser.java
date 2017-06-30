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

package nl.thehyve.ocdu.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Shadow copy of OC user. Required to bind OcEnvironment and OC-ws password hash with user HTTP session.
 * Created by piotrzakrzewski on 18/04/16.
 */

@Entity
public class OcUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String ocEnvironment;
    private String username;


    public OcUser() {
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    /*public List getUploadSessions() {
        return uploadSessions;
    }

    public void setUploadSessions(List uploadSessions) {
        this.uploadSessions = uploadSessions;
    }
*/
    public String getOcEnvironment() {
        return ocEnvironment;
    }

    public void setOcEnvironment(String ocEnvironment) {
        this.ocEnvironment = ocEnvironment;
    }
}
