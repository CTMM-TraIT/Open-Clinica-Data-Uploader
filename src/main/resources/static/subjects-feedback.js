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

/**
 * Created by bo on 5/13/16.
 */

var displayMessages = function displayMessages(data) {
    makeProgressSectionVisible(false);
    if (data.length == 0) {
        var html = '<div class="alert alert-success"> <strong>Subject validation is successful!</strong></div>';
        $('#feedback-tables').append(html);
    }//if
    else {
        $('#feedback-errors').empty();
        $('#feedback-warnings').empty();
        var numberOfErrors = 0;
        for (var i = 0; i < data.length; i++) {
            var fb = data[i];
            if (fb['error'] == true) {
                numberOfErrors++;
            }
        }
        var numberOfWarnings = data.length - numberOfErrors;
        if (numberOfErrors != 0) {
            var error_word = numberOfErrors + ' errors found.';
            var html_title = '<h3><span> <strong>' + error_word + '</strong> </span></h3>';
            $('#feedback-errors').append(html_title);

            for (var i = 0; i < data.length; i++) {
                var fb = data[i];
                if (fb['error'] == true) {
                    var msg = fb['message'];
                    var vals = fb['offendingValues'];
                    var errorid = "error" + i;
                    var middlepart = '<div class="panel-heading"><h4 class="panel-title"><a data-toggle="collapse" href="#' + errorid + '"> ' + msg + '</a></h4></div>';
                    var listpart = '<ul class="list-group">';

                    for (var j = 0; j < vals.length; j++) {
                        listpart += '<li class="list-group-item">' + vals[j] + '</li>'
                    }
                    listpart += '</ul>';
                    middlepart += '<div id="' + errorid + '" class="panel-collapse collapse in">' + listpart + '</div>';
                    var html = '<div class="panel-group"><div class="panel panel-default">' + middlepart + '</div></div>'
                    $('#feedback-errors').append(html);
                }
            }//for
        }

        if (numberOfWarnings != 0) {
            var error_word = numberOfWarnings + '';
            if (numberOfWarnings == 1) {
                error_word += ' warning found';
            } else {
                error_word +=' warnings found';
            }
            var html_title = '<h3><span>' + error_word + '</span></h3>';
            $('#feedback-warnings').append(html_title);
            for (var i = 0; i < data.length; i++) {
                var fb = data[i];
                if (fb['error'] == false) {
                    var msg = fb['message'];
                    var vals = fb['offendingValues'];
                    var successId = "success" + i;
                    var middlepart = '<div class="panel-heading"><h4 class="panel-title"><a data-toggle="collapse" href="#' + successId + '"> ' + msg + '</a></h4></div>';
                    var listpart = '<ul class="list-group">';

                    for (var j = 0; j < vals.length; j++) {
                        listpart += '<li class="list-group-item">' + vals[j] + '</li>'
                    }
                    listpart += '</ul>';
                    middlepart += '<div id="' + successId + '" class="panel-collapse collapse in">' + listpart + '</div>';
                    var html = '<div class="panel-group"><div class="panel panel-default">' + middlepart + '</div></div>'
                    $('#feedback-warnings').append(html);
                }
            }//for
        }
    }//else
};

function feedbackNext() {
    makeProgressSectionVisible(false);
    $.ajax({
        url: baseApp + "/submission/update",
        type: "POST",
        data: {step: "events"},
        success: function () {
            //handle subject file upload
            window.location.href = baseApp + "/views/events";
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.log(jqXHR.status + " " + textStatus + " " + errorThrown);
            var html = '<div class="alert alert-danger">Submission update has failed, please refresh the page.</div>';
            $(html).insertBefore('#data-feedback-back-btn');
        }
    });
}

function backBtnHandler() {
    window.location.href = baseApp + "/views/subjects";
}

function makeProgressSectionVisible(visible) {
    if (visible === false) {
        document.getElementById('progression-section').style.display = 'none';
    }
    else {
        document.getElementById('progression-section').style.display = 'inline';
    }
}

$(document).ready(function () {
    _SESSION_CONFIG = JSON.parse(localStorage.getItem("session_config"));
    _CURRENT_SESSION_NAME = localStorage.getItem("current_session_name");

    if (_SESSION_CONFIG[_CURRENT_SESSION_NAME]['NEED_TO_VALIDATE_SUBJECTS']) {
        //waiting for the ajax call
        makeProgressSectionVisible(true);

        $.ajax({
            url: baseApp + "/validate/patients",
            type: "GET",
            success: displayMessages,
            error: function (jqXHR, textStatus, errorThrown) {
                console.log(jqXHR.status + " " + textStatus + " " + errorThrown);
                makeProgressSectionVisible(false);
                var html = '<div class="alert alert-danger">Subject validation has failed.</div>';
                $(html).insertBefore('#data-feedback-back-btn');
            }
        });
    }
    else {
        var html = '<div class="alert alert-success"> <strong>All subjects in the data file are already registered in OpenClinica, validation is not required. Click Next to proceed.</strong></div>';
        $('#feedback-tables').append(html);
    }

});




