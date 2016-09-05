/**
 * Created by bo on 5/12/16.
 */

var template_str;
var is_template_empty = false;;

$(document).ready(function () {
    _SESSION_CONFIG = JSON.parse(localStorage.getItem("session_config"));
    _CURRENT_SESSION_NAME = localStorage.getItem("current_session_name");
    makeProgressSectionVisible(true)
    next_btn();
    check_new_patients(true);
});

function makeProgressSectionVisible(visible) {
    if (visible === false) {
        document.getElementById('progression-section').style.display = 'none';
    }
    else {
        document.getElementById('progression-section').style.display = 'inline';
    }
}

function check_new_patients(toRegisterSite) {
    $('#template_error').remove();

    $.ajax({
        url: baseApp + "/template/get-subject-template",
        type: "GET",
        data: {registerSite: toRegisterSite},
        success: function (template) {
            makeProgressSectionVisible(false);
            template_str = template;
            if (template.length > 1) {
                provide_template_download();
                provide_filled_template_upload();
            }
            else {
                var html = "<div id='template_error' class='alert alert-success'><strong>All subjects in the data file are already registered in OpenClinica; validation is not required. Click Next to proceed.</strong></div>";
                $(html).insertBefore('#subject-back-btn');
                is_template_empty = true;
                _SESSION_CONFIG[_CURRENT_SESSION_NAME]['NEED_TO_VALIDATE_SUBJECTS'] = false;
                update_session_config(_SESSION_CONFIG);
            }
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.log(jqXHR.status + " " + textStatus + " " + errorThrown);
            var html = "<div id='template_error' class='alert alert-error'>The retrieval of template has failed.</div>"
            $(html).insertBefore('#subject-back-btn');
        }
    });
}


function provide_template_download() {
    var info = '<h4>&#9755; One or more subjects in the data file are not yet present in OpenClinica. These subjects should be registered with a "subject registration template", which can be downloaded<button id="download-subject-template-btn" class="btn btn-link btn-lg text-left" style="text-align: left"> here</button></h4><div id="template-download-anchor"></div><hr>';
    $(info).insertBefore('#subject-back-btn');

    $('#download-subject-template-btn').click(function () {
        var blob = new Blob(template_str, {type: "text/plain;charset=utf-8"});
        saveAs(blob, "subject-registration-template.txt");
    });
}

function provide_filled_template_upload() {
    var html = '<h4>&#9755; Once you have filled out the template, select it using the file chooser below.</h4> <form id="upload-subject-template-form" class="form-horizontal"><input id="upload-subject-template-input" type="file" name="uploadPatientData" accept="*" /></form> <span id="message-board"></span> <hr>';
    $(html).insertBefore('#subject-back-btn');
}

function next_btn() {
    var html = '<button type="button" class="btn btn-primary aligned-btn-primary" id="subject-back-btn">Back</button>&nbsp;' +
        '<button type="button" class="btn btn-primary aligned-btn-primary" id="subject-next-btn">Next</button>';
    $('#subject-registration-div').append(html);
    $('#subject-back-btn').click(function () {
        window.location.href = baseApp + "/views/feedback-data";
    });

    $('#subject-next-btn').click(function () {
        if(!is_template_empty) {
            upload_subjects();
        }
        else{
            if (_SESSION_CONFIG[_CURRENT_SESSION_NAME]['NEED_TO_VALIDATE_SUBJECTS'] === true) {
                window.location.href = baseApp + "/views/feedback-subjects";
            }
            else {
                window.location.href = baseApp + "/views/events";
            }
        }
    });

}

function upload_subjects() {
    makeProgressSectionVisible(true);
    $('#message-board').empty();
    $.ajax({
        url: baseApp + "/upload/subjects",
        type: "POST",
        data: new FormData($("#upload-subject-template-form")[0]),
        enctype: 'multipart/form-data',
        processData: false,
        contentType: false,
        success: function (fileFormatErrors) {
            if (fileFormatErrors.length == 0) {
                update_submission();
            }
            else {
                log_errors(fileFormatErrors);
            }
        },
        error: function (jqXHR, textStatus, errorThrown) {
            makeProgressSectionVisible(false);
            console.log(jqXHR.status + " " + textStatus + " " + errorThrown);
            $('#message-board').append('<div class="alert-danger">Subject upload fails. Please check the format of the subject registration file, and upload again.</div>')
        }

    });
}


function update_submission() {
    $('#template_error').remove();

    $.ajax({
        url: baseApp + "/submission/update",
        type: "POST",
        data: {step: "feedback-subjects"},
        success: function () {
            makeProgressSectionVisible(false);
            //handle subject file upload
            window.location.href = baseApp + "/views/feedback-subjects";
        },
        error: function (jqXHR, textStatus, errorThrown) {
            makeProgressSectionVisible(false);
            console.log(jqXHR.status + " " + textStatus + " " + errorThrown);
            var html = "<div id='template_error' class='alert alert-warning'>The update of submission has failed.</div>"
            $('#subject-registration-div').append(html);
        }
    });
}

function log_errors(errors) {
    var info = '<div class="alert alert-danger"><ul>';
    errors.forEach(function (error) {
        var errDiv = '<li><span>' + error.message + '</span></li>';
        info += errDiv;
    });
    info += '</div></ul>';
    $("#message-board").append(info);
    makeProgressSectionVisible(false);
}
