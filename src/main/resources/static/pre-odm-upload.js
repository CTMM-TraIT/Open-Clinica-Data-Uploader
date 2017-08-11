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
 * Created by jacob on 6/30/16. Based on previous work by bo
 */
var displayMessages = function displayMessages(data) {
    makeProgressSectionVisible(false);
    $('#loading_div').remove();
    if (data.length == 0) {
        var html = '<div class="alert alert-success"> <strong>No remaining validation errors and problems found!</strong></div>';
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
    }
};

function feedbackNext() {
    $.ajax({
        url: baseApp + "/submission/update",
        type: "POST",
        timeout: 0,
        data: {step: "odm-upload"},
        success: function () {
            window.location.href = baseApp + "/views/odm-upload";
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.log(jqXHR.status + " " + textStatus + " " + errorThrown);
        }
    });
}

function backBtnHandler() {
    window.location.href = baseApp + "/views/feedback-events";
}

function makeProgressSectionVisible(visible) {
    if (visible === true) {
        document.getElementById('progression-section').style.display = 'inline';
        document.getElementById('feedback-errors').style.display = 'none';
        document.getElementById('feedback-warnings').style.display = 'none';
    }
    else {
        document.getElementById('progression-section').style.display = 'none';
        document.getElementById('feedback-errors').style.display = 'inline';
        document.getElementById('feedback-warnings').style.display = 'inline';
    }
}


makeProgressSectionVisible(true);
$.ajax({
    url: baseApp + "/odm/pre-odm-upload-overview",
    type: "GET",
    timeout: 0,
    success: displayMessages,
    error: function (jqXHR, textStatus, errorThrown) {
        console.log(jqXHR.status + " " + textStatus + " " + errorThrown);
    }
});

