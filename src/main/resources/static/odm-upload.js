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
 * Javascript to control the ODM-upload
 * Created by jacob on 6/28/16.
 */


$(document).ready(function () {
    makeProgressSectionVisible(false);
    document.getElementById('contact-information').style.display = 'none';
});

function makeProgressSectionVisible(visible) {
    if (visible === false) {
        document.getElementById('progression-section').style.display = 'none';
    }
    else {
        document.getElementById('progression-section').style.display = 'inline';
    }
}

function update_submission() {
    $.ajax({
        url: baseApp + "/submission/update",
        type: "POST",
        timeout: 0,
        data: {step: "final"},
        success: function () {
            makeProgressSectionVisible(false);
            window.location.href = baseApp + "/views/final";
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.log(jqXHR.status + " " + textStatus + " " + errorThrown);
            var html = '<div class="alert alert-danger">Submission update failed, please try Upload again.</div>';
            $(html).insertBefore('#odm-upload-back-btn');
        }
    });
}

function performODMUpload() {
    var form = document.getElementById('upload-odm-template-form');

    var dataString = $(form).serialize();

    $('#odm-upload-div').remove();
    $('#buttonDiv').hide();
    makeProgressSectionVisible(true);
    $('#odm-upload-proceed-btn').hide();
    $.ajax({
        url: baseApp + "/odm/upload",
        type: "POST",
        timeout: 0,
        data: dataString,
        enctype: 'application/x-www-form-urlencoded',
        success: function (msg) {
            makeProgressSectionVisible(false);
            $('#uploadOptions').remove();
            $('#buttonDiv').show();
            if (msg.length > 0) {
                var errorList = [];
                var warningList = [];
                var notificationList = [];
                var len = msg.length;
                for (i = 0; i < len; ++i) {
                    var message = msg[i];
                    if (message.error) {
                        errorList.push(message);
                    }
                    if (message.warning) {
                        warningList.push(message);
                    }
                    if (message.notification) {
                        notificationList.push(message);
                    }
                }

                if ((errorList) && (errorList.length > 0)) {
                    var errorInfo = createMessageDiv(errorList, 'errorInfoDiv', 'alert alert-danger', 'Failures');
                    $('#upload-result').append(errorInfo);
                }
                if ((warningList) && (warningList.length > 0)) {
                    var warningInfo = createMessageDiv(warningList, 'warningInfo', 'alert alert-warning', 'Warnings');
                    $('#upload-result').append(warningInfo);
                }

                if ((notificationList) && (notificationList.length > 0)) {
                    var notificationInfo = createMessageDiv(notificationList, 'notificationInfoDiv', 'alert alert-success', 'Success');
                    $('#upload-result').append(notificationInfo);
                }
            }
            $('#odm-upload-proceed-btn').remove();
            $('#odm-upload-back-btn').remove();
        },
        error: function (jqXHR, textStatus, errorThrown) {
            makeProgressSectionVisible(false);
            console.log(jqXHR.status + " " + textStatus + " " + errorThrown);
            document.getElementById('contact-information').style.display = 'inline';
            var html = '<div class="alert alert-danger">ODM Upload failed: ' + textStatus + ' </div>';
            $('#upload-result').append(html);
        }

    });
}

function createMessageDiv(messageList, divID, divCSSClass, divTitle) {
    var errorInfo = '<div class="' + divCSSClass + '" id="' + divID + '"><h4>'+ divTitle + '</h4><ul>';
    messageList.forEach(function (error) {
        var errDiv = '<li><span>' + error.message;
        if ((error.offendingValues) && (error.offendingValues.length > 0))
        {
            errDiv += ': ' + error.offendingValues;
        }
        errDiv += '</span></li>';
        errorInfo += errDiv;
    });
    errorInfo += '</div></ul>';
    return errorInfo;
}

function log_errors(errors) {
    var info = '<div class="alert alert-danger"><ul>';
    errors.forEach(function (error) {
        var errDiv = '<li><span>' + error.message + '</span></li>';
        info += errDiv;
    });
    info += '</div></ul>';
    $("#message-board").append(info);
}

function odmUploadNextBtnHandler() {
    performODMUpload();
}

function odmUploadBackBtnHandler() {
    window.location.href = baseApp + "/views/pre-odm-upload";
}
