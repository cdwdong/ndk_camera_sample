var express = require('express');
var router = express.Router();

/* GET home page. */
router.get('/', function(req, res, next) {
  if(req.headers.accept && req.headers.accept == 'text/event-stream') {
    
  } else {
    res.writeHead(404);
    res.end();
  }

});
function sendSSE(req, res) {
    res.writeHead(200, {
        'Content-Type': 'text/event-stream',
        'Cache-Control': 'no-cache',
        'Connection': 'keep-alive'
    });

    var id = (new Date()).toLocaleTimeString();
    
}
function constructSSE(res, id, data) {
    res.write('id: ' + id + '\n');
    res.write('data: ' + data + '\n\n');
}


module.exports = router;
