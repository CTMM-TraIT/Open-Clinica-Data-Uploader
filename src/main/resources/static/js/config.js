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

var baseApp = "/ocdi";
var USERNAME = "";
var _SESSIONS = [];
var _CURRENT_SESSION_NAME = "";
var _SESSION_CONFIG = {};


function init_session_config(session_name) {
    if(!(session_name in _SESSION_CONFIG)) {
        var obj = {};
        obj['MAPPING_FILE_ENABLED'] = false;//default
        obj['NEED_TO_VALIDATE_SUBJECTS'] = true;//default
        obj['NEED_TO_VALIDATE_EVENTS'] = true;//default

        _SESSION_CONFIG[session_name] = obj;
        localStorage.setItem("session_config", JSON.stringify(_SESSION_CONFIG));
    }
}


function update_session_config(session_config) {
    localStorage.setItem("session_config", JSON.stringify(session_config));
}
