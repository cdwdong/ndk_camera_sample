var fManager = require('../src/filemanage');
var onConnect = function(io) {
    return (function(socket) {
        console.log(new Date().toLocaleString() + " client connected");
        socket.on("request get setting data", function(msg) {
            fManager.readData(function (data, status) {
                if(!status) {
                    //FAIL
                }
                data.status = status;
                socket.emit("response setting data", data);
                return;
            });
        });
        socket.on("request post setting data", function(msg) {
            fManager.writeData(msg);
        });
        socket.on("request capture camera", function () {
            socket.broadcast.emit("response capture camera");
        });
        socket.on("request send picture", function (msg) {
            socket.broadcast.emit("response send picture", msg);
        });
        
    });
};


var onDisconnect = function(io) {
    console.log(new Date().toLocaleString() + " client disconnected");
    return function(socket) {
        console.log("finished");
    };
};

var onForceDisconnect = function(io) {
    return function(socket) {
        socket.disconnect();
    };
};

module.exports = {
    onConnect: onConnect,
    onDisConnect: onDisconnect,
    onForceDisconnect: onForceDisconnect
}