/**
 * Created by jacob on 6/30/16. Based on previous work by bo
 */
var displayMessages = function displayMessages(data) {
    $('#loading_div').remove();

    $('#feedback-errors').empty();
    $('#feedback-success').empty();
    var numberOfErrors = 0;
    for (var i = 0; i < data.length; i++) {
        var fb = data[i];
        if (fb['error'] == true) {
            numberOfErrors++;
        }
    }
    var numberOfSuccess =  data.length - numberOfErrors;
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

    if (numberOfSuccess != 0) {
        var message = 'Data successfully uploaded for ' + numberOfSuccess + 'subjects.';
        var html_title = '<h3><span> <strong>' + message + '</strong> </span></h3>';
        $('#feedback-success').append(html_title);

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
                $('#feedback-success').append(html);
            }
        }//for
    }
};


function backBtnHandler() {
    window.location.href = baseApp + "/views/odn-upload";
}


$.ajax({
    url: baseApp + "/view/final",
    type: "GET",
    success: displayMessages,
    error: function (jqXHR, textStatus, errorThrown) {
        console.log(jqXHR.status + " " + textStatus + " " + errorThrown);
    }
});

