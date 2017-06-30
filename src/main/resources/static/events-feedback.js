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
    if(data.length == 0) {
        var html = '<div class="alert alert-success"> <strong>Event validation is successful!</strong></div>';
        $('#feedback-tables').append(html);
    }//if
    else {
        $('#feedback-tables').empty();
        var error_word = 'errors'; if(data.length == 1) error_word = 'error';
        var html_title = '<h2 class="form-step-title">'+data.length +' '+error_word+' found ...</h2>';
        $('#feedback-tables').append(html_title);

        for (var i = 0; i < data.length; i++) {
            var fb = data[i];
            var msg = fb['message'];
            var vals = fb['offendingValues'];
            var errorid = "error"+i;
            var middlepart = '<div class="panel-heading"><h4 class="panel-title"><a data-toggle="collapse" href="#'+errorid+'"> '+msg+'</a></h4></div>';
            var listpart = '<ul class="list-group">';

            for (var j = 0; j < vals.length; j++) {
                listpart += '<li class="list-group-item">' + vals[j]+'</li>'
            }
            listpart += '</ul>';
            middlepart += '<div id="'+errorid+'" class="panel-collapse collapse in">'+listpart+'</div>';
            var html = '<div class="panel-group"><div class="panel panel-default">'+middlepart+'</div></div>'
            $('#feedback-tables').append(html);
        }//for
    }//else
};

function feedbackNext() {
    $.ajax({
        url: baseApp + "/submission/update",
        type: "POST",
        data: {step: "pre-odm-upload"},
        success: function () {
            window.location.href = baseApp + "/views/pre-odm-upload";
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.log(jqXHR.status+" "+textStatus+" "+errorThrown);
            var html = "<div class='label label-danger'>Submission update has failed, please refresh the page.</div>";
            $(html).insertBefore('#data-feedback-back-btn');
        }
    });
}

function backBtnHandler() {
    window.location.href = baseApp + "/views/events";
}


function makeProgressSectionVisible(visible) {
    if (visible === true) {
        document.getElementById('progression-section').style.display = 'inline';
        document.getElementById('feedback-tables').style.display = 'none';
    }
    else {
        document.getElementById('progression-section').style.display = 'none';
        document.getElementById('feedback-tables').style.display = 'inline';
    }
}

$(document).ready(function () {
    contains_events_unscheduled = true;
    _SESSION_CONFIG = JSON.parse(localStorage.getItem("session_config"));
    _CURRENT_SESSION_NAME = localStorage.getItem("current_session_name");

    if(_SESSION_CONFIG[_CURRENT_SESSION_NAME]['NEED_TO_VALIDATE_EVENTS']) {
        //waiting for the ajax call
        makeProgressSectionVisible(true);
        $.ajax({
            url: baseApp+"/validate/events",
            type: "GET",
            success: displayMessages,
            cache: false,
            error: function (jqXHR, textStatus, errorThrown) {
                //window.location.href = baseApp + "/views/data";
            }
        });
    }
    else {
        var html = '<div class="alert alert-success"> <strong>No event needs to be scheduled, click Next to proceed.</strong></div>';
        $('#feedback-tables').append(html);
    }
});
