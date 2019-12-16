var fs = require('fs');
const FAIL = 1;
const SUCCESS = 0;
const PATH = __dirname + "/../data/setting_data.json";
const DIRPATH = '/'+PATH.split('/').slice(0, PATH.split('/').length-1).join('/');

var settingDataManage = {
    writeData: function (wData) {
        var fData = {};
        if(!(wData.filter && wData.detect)) {
            return FAIL;
        }
        fData.filter = wData.filter;
        fData.detect = wData.detect;
        if (!fs.existsSync(DIRPATH)) {
            fs.mkdirSync(DIRPATH);
        }
        fs.writeFile(PATH, 
            JSON.stringify(fData, null, '\t'), function (err, data) {
                if(err) {
                    console.log("fail: file save");
                    return FAIL;
                }
                console.log("post: filter=" + fData.filter + " detect=" + fData.detect);
                return SUCCESS;
        });
    },

    readData: function (callback) {
        var rData={};
        fs.readFile(PATH, function(err, data) {
            if(err) {
                console.log("fail: file read");
                callback(rData, FAIL);
                return FAIL;
            }
            try {
                var jsondata = JSON.parse(data);
            } catch (error) {
                callback(rData, FAIL);
                return FAIL;
            }
            for(var key in jsondata) {
                rData[key] = jsondata[key];
            }
            callback(rData, SUCCESS);
            return SUCCESS;
          });
    },
    // watchFile: function (path, callback) {
    //     fs.watchFile(path, callback);
    // }
    watchFile: function (path, callback) {
        fs.watch(path, function (eventType, filename) {
            if('change' === eventType) {
                callback();
            }
        });
    }

}
module.exports = settingDataManage;