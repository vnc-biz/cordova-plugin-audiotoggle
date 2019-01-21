var exec = require('cordova/exec');

exports.BLUETOOTH = 'bluetooth';
exports.SPEAKER = 'speaker';
exports.EARPIECE = 'earpiece';
exports.NORMAL = 'normal';
exports.RINGTONE = 'ringtone';

exports.setAudioMode = function (mode) {
	cordova.exec(null, null, 'AudioTogglePlugin', 'setAudioMode', [mode]);
};

exports.setBluetoothScoOn = function (toggle) {
	cordova.exec(null, null, 'AudioTogglePlugin', 'setBluetoothScoOn', [toggle]);
};

exports.setSpeakerphoneOn = function (toggle) {
	cordova.exec(null, null, 'AudioTogglePlugin', 'setSpeakerphoneOn', [toggle]);
};

exports.getOutputDevices = function (callback) {
	cordova.exec(callback, null, 'AudioTogglePlugin', 'getOutputDevices', []);
};

exports.on = function (eventName, callback) {
   cordova.exec(callback, null, 'AudioTogglePlugin', 'registerListener', [eventName]);
};

exports.getAudioMode = function (callback) {
  cordova.exec(callback, null, 'AudioTogglePlugin', 'getAudioMode', []);
};

exports.isBluetoothScoOn = function (callback) {
	cordova.exec(callback, null, 'AudioTogglePlugin', 'isBluetoothScoOn', []);
};

exports.isSpeakerphoneOn = function (callback) {
	cordova.exec(callback, null, 'AudioTogglePlugin', 'isSpeakerphoneOn', []);
};