var exec = require('cordova/exec');

var HuaweiPushNotification = function () {}
HuaweiPushNotification.prototype.isAndroidDevice = function(){
    return device.platform == 'Android';
}
// 获取到token
HuaweiPushNotification.prototype.tokenRegistered = function (token) {
    try {
        this.receiveRegisterResult = token;
        cordova.fireDocumentEvent('HuaweiPushNotification.receiveRegisterResult', this.receiveRegisterResult);
    } catch(exception) {
        console.log('HuaweiPushNotification:tokenRegistered ' + exception);
    }
}
// 透传消息
HuaweiPushNotification.prototype.pushMsgReceived = function (msg) {
    try {
        msg.extras = JSON.parse(msg.extras);
        this.receiveRegisterResult = msg;
        cordova.fireDocumentEvent('HuaweiPushNotification.pushMsgReceived', this.receiveRegisterResult);
    } catch(exception) {
        console.log('HuaweiPushNotification:pushMsgReceived ' + exception);
    }
}
HuaweiPushNotification.prototype.notificationOpened = function (msg) {
    try {
        msg.extras = JSON.parse(msg.extras);
        this.receiveRegisterResult = msg;
        cordova.fireDocumentEvent('HuaweiPushNotification.notificationOpened', this.receiveRegisterResult);
    } catch(exception) {
        console.log('HuaweiPushNotification:notificationOpened ' + exception);
    }
}
HuaweiPushNotification.prototype.errorFound = function (msg) {
    try {
        msg.extras = JSON.parse(msg.extras);
        this.receiveRegisterResult = msg;
        cordova.fireDocumentEvent('HuaweiPushNotification.errorFound', this.receiveRegisterResult);
    } catch(exception) {
        console.log('HuaweiPushNotification:errorFound ' + exception);
    }
}
HuaweiPushNotification.prototype.init = function(success, error) {
    if (this.isAndroidDevice()) {
        exec(success, error, "HuaweiPushNotification", "init", []);
    }
};
HuaweiPushNotification.prototype.stop = function(success, error) {
    if (this.isAndroidDevice()) {
        exec(success, error, "HuaweiPushNotification", "stop", []);
    }
};

module.exports = new HuaweiPushNotification();
