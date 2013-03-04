/*
This file contains the scripts to control the objects on the dashboard page
*/

/*
This section gets the latest events and shows them in a table
*/
$.getJSON("/get?id=latestEvents", function(data){
  //format timestamps
  for (var i = 0; i < data.length; i++) {
    var date = new Date(data[i][3]);
    data[i][3] = date.toLocaleString();
  }

  //make table
  $('#latestEvents').dataTable( {
    "aaData": data,
    "bLengthChange": false,
    "bPaginate": false,
    "bSort": false,
    "bFilter": false,
    "bInfo": false,
    "aoColumns": [
    { "sTitle": "Event ID", "sClass": "control", "bVisible": false },
    { "sTitle": "Router IP" , "sClass": "control"},
    { "sTitle": "Type", "sClass": "control" },
    { "sTitle": "Time Submitted", "sClass": "control" }
    ]
} );

});

/*
This section gets the running map reduce jobs and shows them in a table
*/
$.getJSON("/get?id=runningJobs", function(data){

  //format timestamps
  for (var i = 0; i < data.length; i++) {
    var date = new Date(data[i][1]);
    data[i][1] = date.toLocaleString();
  }

  //make table
  $('#runningJobs').dataTable( {
    "aaData": data,
    "bLengthChange": false,
    "bPaginate": false,
    "bSort": false,
    "bFilter": false,
    "bInfo": false,
    "aoColumns": [
    { "sTitle": "Router IP", "sClass": "control", "sWidth": "10px"},
    { "sTitle": "Last Seen", "sClass": "control", "sWidth": "140px"},
    { "sTitle": "Progress" , "sClass": "control",

    "mDataProp": function (source, type, val) {
        ratio = (source[2] / source[3]) * 100;
        return '<div class="progress progress-success progress-striped"><div class="bar" style="width: '+ratio+'%"></div></div>';}

    },
    { "sTitle": "Max Jobs", "sClass": "control", "bVisible": false}
    ]
  } );

});