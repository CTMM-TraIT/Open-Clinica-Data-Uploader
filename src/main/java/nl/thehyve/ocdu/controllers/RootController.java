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

package nl.thehyve.ocdu.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Starting view for authenticated user.
 *
 * Created by piotrzakrzewski on 10/05/16.
 */
@Controller
public class RootController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String data() {
        return "data";
    }
}
