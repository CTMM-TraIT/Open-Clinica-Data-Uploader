/**
 * Created by bo on 5/7/16.
 */


var displayMessages = function displayMessages(data) {
    $('#progression-section').remove();
    if(data.length == 0) {
        var html = '<div class="alert alert-success"> <strong>Data validation is successful!</strong></div>';
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


            var error_word = 'errors';
            if (data.length == 1) error_word = 'error';
            var html_title = '<h3><span>' + numberOfErrors + ' ' + error_word + ' found... </span></h3>';
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

function feedbackDataNext() {
    $.ajax({
        url: baseApp + "/submission/update",
        type: "POST",
        data: {step: "subjects"},
        success: function () {
            window.location.href = baseApp + "/views/subjects";
        },
        error: function (jqXHR, textStatus, errorThrown) {
            makeProgressSectionVisible(false);
            console.log(jqXHR.status+" "+textStatus+" "+errorThrown);
            window.location.href = baseApp + "/views/feedback-data";
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

function backBtnHandler() {
    window.location.href = baseApp + "/views/mapping";
}

//waiting for the ajax call
makeProgressSectionVisible(true);
$.ajax({
    url: baseApp+"/validate/data",
    type: "GET",
    cache: false,
    success: displayMessages,
    error: function (jqXHR, textStatus, errorThrown) {
        makeProgressSectionVisible(false);
        console.log("Data Validation fails.");
        window.location.href = baseApp + "/views/data";
    }
});

