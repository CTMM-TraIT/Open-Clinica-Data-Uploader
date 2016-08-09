/**
 * Javascript to control the ODM-upload
 * Created by jacob on 6/28/16.
 */

var loading_html;

$(document).ready(function () {
    generateUploadSettingsHTML();
});

function generateUploadSettingsHTML() {
    var html = '<span id="uploadOptions"><h3>Upload options</h3>' +
        '<h4><div>Please select the status after upload and the status of existing CRF\'s which can be overwritten.</div></h4>' +
        '<hr>' +
        '<div><strong>CRF Status After Upload</strong></div>' +
        '<div class="radio">' +
        '<label><input id="statusAfterUpload_1" type="radio" name="statusAfterUpload" value="initial data entry" checked>Data Entry Started</label>' +
        '</div> ' +
        '<div class="radio"> ' +
        '<label><input id="statusAfterUpload_2" type="radio" name="statusAfterUpload" value="complete">Data Entry Complete</label>' +
        '</div>' +
        '<hr>' +
        '<div><strong>Upload to CRF with status (existing CRF\'s will be overwritten)</strong></div>' +
        '<div class="checkbox">' +
        '<label><input id="overwriteStatus_1" type="checkbox" name="overwriteStatus" value="overwriteStatus_notStarted" checked>Not started</label>' +
        '</div> ' +
        '<div class="checkbox"> ' +
        '<label><input id="overwriteStatus_2" type="checkbox" name="overwriteStatus" value="overwriteStatus_initialDataEntry">Data Entry Started</label>' +
        '</div>' +
        '<div class="checkbox"> ' +
        '<label><input id="overwriteStatus_3" type="checkbox" name="overwriteStatus" value="overwriteStatus_dataEntryComplete">Data Entry Complete</label>' +
        '</div><hr></span>';
    
    $(html).insertBefore('#odm-upload-back-btn');
}

function update_submission() {
    $.ajax({
        url: baseApp + "/submission/update",
        type: "POST",
        data: {step: "final"},
        success: function () {
            $('#loading_div').remove();
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
    $('#loading_div').remove();
    $(loading_html).insertBefore('#odm-upload-back-btn');
    $.ajax({
        url: baseApp + "/odm/upload",
        type: "POST",
        data: new FormData($("#upload-odm-template-form")[0]),
        enctype: 'multipart/form-data',
        processData: false,
        contentType: false,
        success: function (msg) {
            $('#loading_div').remove();
            $('#uploadOptions').remove();
            if (msg.length > 0) {
                var errorList = [];
                var nonErrorList = [];
                var len = msg.length;
                for ( i = 0; i < len; ++i ) {
                    var message = msg[i];
                    if (message.error) {
                        errorList[i] = message;
                    }
                    else {
                        nonErrorList[i] = message;
                    }
                };
                var nonErrorInfo = createMessageDiv(nonErrorList, 'nonErrorInfoDiv', 'alert alert-success', 'Success');
                $(nonErrorInfo).insertBefore('#odm-upload-back-btn');

                var errorInfo = createMessageDiv(errorList, 'errorInfoDiv', 'alert alert-danger', 'Failures');
                $(errorInfo).insertAfter('#nonErrorInfoDiv');
            }else {
                console.log("Upload ODM successfully");
                update_submission();
            }
        },
        error: function (jqXHR, textStatus, errorThrown) {
            $('#loading_div').remove();
            console.log(jqXHR.status + " " + textStatus + " " + errorThrown);
            var html = '<div class="alert alert-danger">ODM Upload failed:' + textStatus + ' </div>';
            $(html).insertBefore('#odm-upload-back-btn');
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
