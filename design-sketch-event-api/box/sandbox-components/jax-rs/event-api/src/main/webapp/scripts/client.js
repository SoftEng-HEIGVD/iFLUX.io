function sendEvents() {
  var event = {
	properties: {
		room: "kitchen",
		temperature: 21.3
	}    
  };
  var events = [];
  events.push(event);
  var url="api/events/";
  $.ajax({
    type: "POST",
    url: "api/events/",
    contentType: "application/json",
    data: JSON.stringify(events)
  }).done(function(data, status) {
    alert(status);
  });
}

function getLoggedEvents() {
  $.get("api/debug/", function(data) {
    console.log(data);
    alert(data.length + " events logged.");
  }, "json");
}

$(function() {
    $("#bSendEvents").click(sendEvents);
    $("#bGetLoggedEvents").click(getLoggedEvents);
});

