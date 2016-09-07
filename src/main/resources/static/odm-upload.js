/**
 * Javascript to control the ODM-upload
 * Created by jacob on 6/28/16.
 */


$(document).ready(function () {
    makeProgressSectionVisible(false);
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
        data: dataString,
        enctype: 'application/x-www-form-urlencoded',
        success: function (msg) {
            makeProgressSectionVisible(false);
            $('#uploadOptions').remove();
            $('#buttonDiv').show();
            if (msg.length > 0) {
                var errorList = [];
                var nonErrorList = [];
                var len = msg.length;
                for (i = 0; i < len; ++i) {
                    var message = msg[i];
                    if (message.error) {
                        errorList[i] = message;
                    }
                    else {
                        nonErrorList[i] = message;
                    }
                }

                if ((errorList) && (errorList.length > 0)) {
                    var errorInfo = createMessageDiv(errorList, 'errorInfoDiv', 'alert alert-danger', 'Failures');
                    $(errorInfo).insertBefore('#odm-upload-back-btn');
                }
                if ((nonErrorList) && (nonErrorList.length > 0)) {
                    var nonErrorInfo = createMessageDiv(nonErrorList, 'nonErrorInfoDiv', 'alert alert-success', 'Success');
                    if ((errorList) && (errorList.length > 0)) {
                        $(nonErrorInfo).insertAfter('#errorInfoDiv');
                    }
                    else {
                        $(nonErrorInfo).insertBefore('#odm-upload-back-btn');
                    }
                }
            }
            $('#odm-upload-proceed-btn').remove();
            $('#odm-upload-back-btn').remove();
        },
        error: function (jqXHR, textStatus, errorThrown) {
            makeProgressSectionVisible(false);
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
