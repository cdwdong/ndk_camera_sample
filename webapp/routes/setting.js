var express = require('express');
var router = express.Router();
var fManager = require('../src/filemanage');

router.post('/', function(req, res, next) {
  var result = {};
  var msg = req.body;
  result.status = fManager.writeData(msg);
  res.json(result);
  
});
router.get('/', function(req, res, next) {
  fManager.readData(function (data, status) {
    // if(status) {
    //     //FAIL
    //     data.status = status;
    //     res.json(data);
    // }
    data.status = status;
    res.json(data);
    return;
  });
});

module.exports = router;