/**
 * Created by bo on 5/12/16.
 */
var contains_events_unscheduled;
var template_arr = [];
var loading_html;


$(document).ready(function () {
    makeProgressSectionVisible(true);
    contains_events_unscheduled = true;
    _SESSION_CONFIG = JSON.parse(localStorage.getItem("session_config"));
    _CURRENT_SESSION_NAME = localStorage.getItem("current_session_name");

    next_btn();
    check_new_events();
});

function check_new_events() {

    $('#template_error').remove();

    $.ajax({
        url: baseApp + "/template/get-event-template",
        type: "GET",
        success: function (template) {
            makeProgressSectionVisible(false);
            template_arr = template;
            if (template.length > 1) {
                provide_event_template_download();
                provide_event_template_upload();
            }
            else {
                var html = "<div id='template_error' class='alert alert-success'><strong>All events in the data file have been scheduled in OpenClinica. Click Next to proceed.</strong></div>";
                $(html).insertBefore('#event-back-btn');
                _SESSION_CONFIG[_CURRENT_SESSION_NAME]['NEED_TO_VALIDATE_EVENTS'] = false;
                update_session_config(_SESSION_CONFIG);
            }
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.log(jqXHR.status + " " + textStatus + " " + errorThrown);
            var html = "<div id='template_error' class='alert alert-error'>The retrieval of template has failed.</div>";
            $(html).insertBefore('#event-back-btn');
        }
    });
}


function provide_event_template_download() {
    var info = '<h4>&#9755; Unscheduled events are found in the dataset. These events should be scheduled with an "event scheduling template", which can be downloaded<button id="download-event-template-btn" class="btn btn-link btn-lg text-left" style="text-align: left">here</button></h4><div id="template-download-anchor"></div><hr>';
    $(info).insertBefore('#event-back-btn');

    $('#download-event-template-btn').click(function () {
        var blob = new Blob(template_arr, {type: "text/plain;charset=utf-8"});
        saveAs(blob, "event-scheduling-template.txt");
    });
}

function provide_event_template_upload() {
    var html = '<h4>&#9755; Once you have filled out the template, select it using the file chooser below.</h4> <form id="upload-event-template-form" class="form-horizontal"><input id="upload-event-template-input" type="file" name="uploadEventFile" accept="*" /></form> <span id="message-board"></span> <hr>';
    $(html).insertBefore('#event-back-btn');
}

function next_btn() {
    var html = '<button type="button" class="btn btn-primary aligned-btn-primary" id="event-back-btn">Back</button>&nbsp;' +
        '<button type="button" class="btn btn-primary aligned-btn-primary" id="event-next-btn">Next</button>';
    $('#event-registration-div').append(html);
    $('#event-back-btn').click(function () {
        window.location.href = baseApp + "/views/feedback-subjects";
    });

    $('#event-next-btn').click(function () {
        if(template_arr.length > 1) {
            upload_event_data();
        }
        else {
            if (_SESSION_CONFIG[_CURRENT_SESSION_NAME]['NEED_TO_VALIDATE_EVENTS'] === true) {
                window.location.href = baseApp + "/views/feedback-events";
            }
            else {
                window.location.href = baseApp + "/views/pre-odm-upload";
            }
        }
    });
}

function makeProgressSectionVisible(visible) {
    if (visible === false) {
        document.getElementById('progression-section').style.display = 'none';
    }
    else {
        document.getElementById('progression-section').style.display = 'inline';
    }
}

function update_submission() {
    $('#template_error').remove();
    $.ajax({
        url: baseApp + "/submission/update",
        type: "POST",
        data: {step: "feedback-events"},
        success: function () {
            console.log("Update submission called successfully");
            makeProgressSectionVisible(false);
            window.location.href = baseApp + "/views/feedback-events";
        },
        error: function (jqXHR, textStatus, errorThrown) {
            makeProgressSectionVisible(false);
            console.log(jqXHR.status + " " + textStatus + " " + errorThrown);
            var html = "<div id='template_error' class='alert alert-danger'>The submission update has failed.</div>"
            $('#subject-registration-div').append(html);
        }
    });
}

function upload_event_data() {
    makeProgressSectionVisible(true);
    $('#template_error').remove();
    $(loading_html).insertAfter('#message-board');
    $('#message-board').empty();
    $.ajax({
        url: baseApp + "/upload/events",
        type: "POST",
        data: new FormData($("#upload-event-template-form")[0]),
        enctype: 'multipart/form-data',
        processData: false,
        contentType: false,
        success: function (fileFormatErrors) {
            console.log(fileFormatErrors);
            if(fileFormatErrors.length == 0) {
                update_submission();
            }
            else{
                log_errors(fileFormatErrors);
            }
        },
        error: function (jqXHR, textStatus, errorThrown) {
            makeProgressSectionVisible(false);
            console.log(jqXHR.status + " " + textStatus + " " + errorThrown);
            var html = "<div id='template_error' class='alert alert-danger'>The upload for event registration has failed.</div>";
            $(html).insertBefore('#event-back-btn');
            // $('#subject-registration-div').append(html);
        }

    });
}

function makeProgressSectionVisible(visible) {
    if (visible === false) {
        document.getElementById('progression-section').style.display = 'none';
    }
    else {
        document.getElementById('progression-section').style.display = 'inline';
    }
}

function log_errors(errors) {
    makeProgressSectionVisible(false);
    var info = '<div class="alert alert-danger"><ul>';
    errors.forEach(function (error) {
        var errDiv = '<li><span>' + error.message + '</span></li>';
        info += errDiv;
    });
    info += '</div></ul>';
    $("#message-board").append(info);
}
