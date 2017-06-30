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

package nl.thehyve.ocdu.repositories;

import nl.thehyve.ocdu.models.OcUser;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by piotrzakrzewski on 28/04/16.
 */
public interface OCUserRepository extends CrudRepository<OcUser, Long> {


    List<OcUser> findByOcEnvironment(String ocEnvironment);

    List<OcUser> findByUsername(String username);

}
