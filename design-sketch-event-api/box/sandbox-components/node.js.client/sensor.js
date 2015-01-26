var Client = require('node-rest-client').Client;
var moment = require('moment');

client = new Client();

var now = moment.utc();
var formatted = now.format('YYYY-MM-DDTHH:mm:ss.SSS') + 'Z';


var event = {
	timestamp: formatted,
  source: "anonymous event source",
  type: "undefined type",
	properties: {
		room: "living room",
		temperature: 22.3
	}
}

var events = [];
events.push(event);


var args = {
  data: events,
  headers:{"Content-Type": "application/json"} 
};

client.post("http://localhost:3000/events/", args, function(data, response) {
	console.log(data);
});


/*
client.post("http://localhost:8080/event-api/api/events/", args, function(data, response) {
	console.log(data);
});
*/