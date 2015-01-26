var express = require('express');
var router = express.Router();

router.logger = {
	history : [],
	push : function(event) {
		this.history.push(event);
	},
	getList : function() {
		return this.history;
	}
};

router.post('/', function(req, res) {
  var events = req.body;
  for (var i=0; i<events.length; i++) {  
	console.log("event: " + events[i].timestamp + " -> " + events[i].type);
    router.logger.push(events[i]);
  }
  res.status(202).send();
});

router.get('/debug', function(req, res) {
	res.send(router.logger.getList());
});


module.exports = router;
