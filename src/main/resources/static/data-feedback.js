/**
 * Created by bo on 5/7/16.
 */


var displayMessages = function displayMessages(data) {
    if(data.length == 0) {
        var html = '<div class="alert alert-success"> <strong>Data validation is successful!</strong></div>';
        $('#feedback-tables').append(html);
    }//if
    else {
        $('#feedback-tables').empty();
        var html_title = '<h3><span> <strong>'+data.length +' errors found. </strong> </span></h3>';
        $('#feedback-tables').append(html_title);

        for (var i = 0; i < data.length; i++) {
            var fb = data[i];
            var msg = fb['message'];
            var vals = fb['offendingValues'];
            var errorid = "error"+i;
            var middlepart = '<div class="panel-heading"><h4 class="panel-title"><a data-toggle="collapse" href="#'+errorid+'">Validation Error: '+msg+'</a></h4></div>';
            var listpart = '<ul class="list-group">';

            for (var j = 0; j < vals.length; j++) {
                listpart += '<li class="list-group-item">'+vals[j]+'</li>'
            }
            listpart += '</ul>';
            middlepart += '<div id="'+errorid+'" class="panel-collapse collapse in">'+listpart+'</div>';
            var html = '<div class="panel-group"><div class="panel panel-default">'+middlepart+'</div></div>'
            $('#feedback-tables').append(html);
        }//for
    }//else
};

function feedbackDataNext() {
    window.location.replace(baseApp + "/views/mapping");
}

$.ajax({
    url: baseApp+"/validate/data",
    type: "GET",
    cache: false,
    success: displayMessages,
    error: function () {
        console.log("Fetching validation errors from the server failed.");
    }
});

//for testing
// d3.json('/data/test-feedback-data.json', function (data) {
//     displayMessages(data);
// });