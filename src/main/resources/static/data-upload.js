/**
 * Created by bo on 4/17/16.
 */
/**
 * Upload the file sending it via Ajax at the Spring Boot server.
 */

var _deleteCurrentSubmission = function() {
    $.ajax({
        url: baseApp + "/submission/delete",
        type: "post",
        data: {},
        success: function () {
            console.log('submission deleted');
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.log(jqXHR.status + " " + textStatus + " " + errorThrown);
        }
    });
};

function displayError(data) {
    hideUploadSection(true);


    var html_title = '<p>Error occurred:</p>';
    $('#message-board').append(html_title);

    var info = '<div class="alert alert-danger"><ul>';

    var errDiv = '<li><span>' + data.responseText + '</span></li>';
    info += errDiv;

    info += '</div></ul>';
    $("#message-board").append(info);


}


function uploadFile() {

    hideUploadSection(false);
    $("#message-board").empty();


    var isSessionNameDefined = ($('#upload-session-input').val() !== "");
    var isDataSelected = ($('#upload-file-input').val() !== "");
    var isMappingSelected = ($('#upload-mapping-input').val() !== "");

    _CURRENT_SESSION_NAME = $('#upload-session-input').val();

    localStorage.setItem("current_session_name", _CURRENT_SESSION_NAME);

    var sessionnames = [];
    for (var i = 0; i < _SESSIONS.length; i++) {
        sessionnames.push(_SESSIONS[i].name);
    }
    if (sessionnames.indexOf(_CURRENT_SESSION_NAME) !== -1) isSessionNameDefined = false;


    var mappingFileUpload = function () {
        if (isMappingSelected) {
            //upload the mapping file and direct to mapping view, also enable the mapping button by MAPPING_FILE_ENABLED = true;
            // MAPPING_FILE_ENABLED = true;
            // var upload_mapping_data = new FormData($("#upload-mapping-form")[0]);
            // $.ajax({
            //     headers: {
            //         'Accept': 'application/json',
            //         'Content-Type': 'application/json'
            //     },
            //     url: baseApp+"/upload/mapping",
            //     type: "POST",
            //     data: upload_mapping_data,
            //     processData: false, // Don't process the files
            //     // contentType: false,
            //     cache: false,
            //     dataType: 'json',
            //     success: function (feedback) {
            //         // Handle upload success
            //         var info = '<span id="mapping-alert" class="alert alert-success">Mapping succesfully uploaded</span>';
            //         $("#message-board").append(info);
            //         isMappingUploaded = true;
            //         if (!isDirected && isDataUploaded) {
            //             // window.location.href = "/mapping");
            //             console.log(feedback);
            //         }
            //     },
            //     error: function (jqXHR, textStatus, errorThrown) {
            //         console.log("Mapping upload to the server failed. HTTP status code:" + jqXHR.status + " " + errorThrown);
            //         // Handle upload error
            //         var info = '<span id="mapping-alert" class="alert alert-danger">Mapping not uploaded</span>';
            //         $("#message-board").append(info);
            //         isMappingUploaded = false;
            //     }
            // });
            // window.setTimeout(function () {
            //     $("#mapping-alert").fadeTo(500, 0).slideUp(500, function () {
            //         $(this).empty();
            //     });
            // }, 3000);
        }
        else {
            //direct to mapping view
            window.location.href = baseApp + "/views/mapping";
        }
    }

    var dataFileUpload = function (current_session) {
        $.ajax({
            url: baseApp + "/upload/data",
            type: "POST",
            data: new FormData($("#upload-file-form")[0]),
            enctype: 'multipart/form-data',
            processData: false,
            contentType: false,
            success: function (fileFormatErrors) {
                if (fileFormatErrors.length === 0) {
                    // Handle upload success
                    // mappingFileUpload();
                    init_session_config(_CURRENT_SESSION_NAME);
                    window.location.href = baseApp + "/views/mapping";
                } else {
                    hideUploadSection(true);
                    var info = '<div class="alert alert-danger"><ul>';
                    fileFormatErrors.forEach(function (error) {
                        var errDiv = '<li><span>' + error.message +' '+ error.offendingValues+'</span></li>';
                        info += errDiv;
                    });
                    info += '</div></ul>';
                    $("#message-board").append(info);

                    //since this is format error, we delete the just created submission
                    _deleteCurrentSubmission();
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                // Handle upload error
                hideUploadSection(true);
                var message = 'Data upload failed. Please check the data format, which should be a plain, tab-delimited file. The size of the file should be less than 10MB. ';
                var info = '<div id="data-alert" class="alert alert-danger">' + message + '</div>';
                $("#message-board").append(info);
                console.log(jqXHR.status + " " + textStatus + " " + errorThrown);
               _deleteCurrentSubmission();
            }
        });
    };

    if (isSessionNameDefined && isDataSelected) {
        $.ajax({
            url: baseApp + "/submission/create",
            enctype: 'multipart/form-data',
            type: "POST",
            data: {name: _CURRENT_SESSION_NAME},
            success: dataFileUpload,
            error: displayError
        });
    }

    if (!isSessionNameDefined || !isDataSelected) {
        hideUploadSection(true);
        $("#message-board").empty();
        if (!isSessionNameDefined) {
            var info = '<span id="message-alert" class="alert alert-danger">Pleaes give your new submission a unique name. </span>';
            $("#message-board").append(info);
        }
        if (!isDataSelected) {
            var info = '<span id="message-alert" class="alert alert-danger">Please select a data file to upload. </span>';
            $("#message-board").append(info);
        }
    }

} //function uploadFile


function hideUploadSection(showOrHide) {
    if (showOrHide === true) {
        document.getElementById('upload-session-name').style.display = 'inline';
        document.getElementById('upload-file-name').style.display = 'inline';
        document.getElementById('data-proceed-btn').style.display = 'inline';
        document.getElementById('session_container').style.display = 'inline';
        document.getElementById('progression-section').style.display = 'none';
    }
    else {
        document.getElementById('upload-session-name').style.display = 'none';
        document.getElementById('upload-file-name').style.display = 'none';
        document.getElementById('data-proceed-btn').style.display = 'none';
        document.getElementById('session_container').style.display = 'none';
        document.getElementById('progression-section').style.display = 'inline';
    }
}

function retrieveSessions() {
    $.ajax({
        url: baseApp + "/submission/all",
        type: "get",
        success: handle_session_retrieval_all,
        error: function (jqXHR, textStatus, errorThrown) {
            console.log(jqXHR.status + " " + textStatus + " " + errorThrown);
            // if(jqXHR.status == 401) {
            //     window.location.href = baseApp+"/views/data";
            // }
        }
    });
}

function zeroPad(value) {
    if (value < 10) {
        return '0' + value;
    }
    return value;
}

function handle_session_retrieval_all(retrieved_sessions) {
    _SESSIONS = retrieved_sessions;
    $("#data-proceed-btn").attr("disabled", false);
    var monthNames = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
    var insertionPoint = document.getElementById("session_container");
    for (var i = 0; i < _SESSIONS.length; i++) {
        var s = _SESSIONS[i];
        var btnid = "s" + (i + 1);
        var d = new Date(s.savedDate);
        var sessionHTML = '<div class="well" id="session_well_' + i + '">' +
            '<button type="button" class="btn btn-primary" id="' + btnid + '" session_index=' + i + '>' + s.name + '</button>' +
            '<p><small>saved on: ' + monthNames[d.getMonth()] + ' ' + d.getDate() + ', ' + d.getFullYear() + ' ' + zeroPad(d.getHours()) + ':' + zeroPad(d.getMinutes()) + ':' + zeroPad(d.getSeconds()) +'</small></p>' +
            '<button type="button" class="btn btn-danger" id="removal_' + btnid + '" session_index=' + i + '>Remove this submission</button></div>';
        insertionPoint.innerHTML += sessionHTML;
        // $('#session_container').append(sessionHTML);
        $('#' + btnid).click(handle_session_retrieval);
        $('#removal_' + btnid).click(handle_session_removal);
    }//for

    //init session_config
    _SESSIONS.forEach(function (session, index) {
        init_session_config(session.name);
    });
}//function handle_session_retrieval_all

function handle_session_retrieval() {
    var ind = $(this).attr('session_index');
    var session = _SESSIONS[ind];
    var sid = session.id;
    _CURRENT_SESSION_NAME = session.name;
    //set the current session
    $.ajax({
        url: baseApp + "/submission/select",
        type: "get",
        data: {sessionId: sid},
        success: function (data) {
            var page;
            //direct user to the selected session
            switch (session.step) {
                case "MAPPING":
                    page = "mapping";
                    break;
                case "FEEDBACK_DATA":
                    page = "feedback-data";
                    break;
                case "SUBJECTS":
                    page = "subjects";
                    break;
                case "FEEDBACK_SUBJECTS":
                    page = "feedback-subjects";
                    break;
                case "EVENTS":
                    page = "events";
                    break;
                case "FEEDBACK_EVENTS":
                    page = "feedback-events";
                    break;
                case "PRE_ODM_UPLOAD":
                    page = "pre-odm-upload";
                    break;
                case "ODM_UPLOAD":
                    page = "odm-upload";
                    break;
                case "FINAL":
                    page = "final";
                    break;
                default:
                    page = "mapping";
            }
            window.location.href = baseApp + "/views/" + page;
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.log(jqXHR.status + " " + textStatus + " " + errorThrown);
        }
    });
}//function handle_session_retrieval

function handle_session_removal() {
    var ind = $(this).attr('session_index');
    var session = _SESSIONS[ind];
    var sid = session.id;
    $.ajax({
        url: baseApp + "/submission/deleteSession",
        type: "post",
        data: {id: sid},
        success: function (data) {
            console.log('deleted session ' + sid);
            $('#session_well_' + ind).remove();
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.log(jqXHR.status + " " + textStatus + " " + errorThrown);
        }
    });
}


function backBtnHandler() {
    window.history.back();
}

$(document).ready(function () {
    $("#data-proceed-btn").attr("disabled", "disabled");

    //retrieve user name
    $.ajax({
        url: baseApp + "/submission/username",
        type: "get",
        success: function (data) {
            USERNAME = data;
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.log(jqXHR.status + " " + textStatus + " " + errorThrown);
        }
    });

    retrieveSessions();
});
