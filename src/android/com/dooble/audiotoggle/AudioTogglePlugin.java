package com.dooble.audiotoggle;

import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.media.AudioManager;
import android.media.AudioDeviceInfo;
import java.util.List;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AudioTogglePlugin extends CordovaPlugin {
  public static final String ACTION_SET_AUDIO_MODE = "setAudioMode";
  public static final String ACTION_SET_BLUETOOTH_ON = "setBluetoothScoOn";
  public static final String ACTION_SET_SPEAKER_ON = "setSpeakerphoneOn";
  public static final String ACTION_GET_OUTPUT_DEVICES = "getOutputDevices";

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    if (action.equals(ACTION_SET_AUDIO_MODE)) {
      if (!setAudioMode(args.getString(0))) {
        callbackContext.error("Invalid audio mode");
        return false;
      }

      return true;
    } else if (action.equals(ACTION_SET_BLUETOOTH_ON)) {
      setBluetoothScoOn(args.getBoolean(0));
      return true;
    } else if (action.equals(ACTION_SET_SPEAKER_ON)) {
      setSpeakerphoneOn(args.getBoolean(0));
      return true;
    } else if (action.equals(ACTION_GET_OUTPUT_DEVICES)) {
      callbackContext.success(getOutputDevices());
      return true;
    }

    callbackContext.error("Invalid action");
    return false;
  }

  public void setBluetoothScoOn(boolean on) {
    final Context context = webView.getContext();
    final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

    audioManager.setBluetoothScoOn(on);
    if (on) {
      audioManager.startBluetoothSco();
    } else {
      audioManager.stopBluetoothSco();
    }
  }

  public void setSpeakerphoneOn(boolean on) {
    final Context context = webView.getContext();
    final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

    audioManager.setSpeakerphoneOn(on);
  }

  public boolean setAudioMode(String mode) {
    final Context context = webView.getContext();
    final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

    if (mode.equals("bluetooth")) {
      audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
      audioManager.setBluetoothScoOn(true);
      audioManager.startBluetoothSco();
      return true;
    } else if (mode.equals("earpiece")) {
      audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
      audioManager.stopBluetoothSco();
      audioManager.setBluetoothScoOn(false);
      audioManager.setSpeakerphoneOn(false);
      return true;
    } else if (mode.equals("speaker")) {
      audioManager.setMode(AudioManager.MODE_NORMAL);
      audioManager.stopBluetoothSco();
      audioManager.setBluetoothScoOn(false);
      audioManager.setSpeakerphoneOn(true);
      return true;
    } else if (mode.equals("ringtone")) {
      audioManager.setMode(AudioManager.MODE_RINGTONE);
      audioManager.setSpeakerphoneOn(false);
      return true;
    } else if (mode.equals("normal")) {
      audioManager.setMode(AudioManager.MODE_NORMAL);
      audioManager.setSpeakerphoneOn(false);
      return true;
    }

    return false;
  }

  public JSONObject getOutputDevices() {
    final Context context = webView.getContext();
    final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

    try {
      AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);

      JSONArray retdevs = new JSONArray();
      for (AudioDeviceInfo dev : devices) {
        if (dev.isSink()) {
          if (dev.getType() != AudioDeviceInfo.TYPE_BUILTIN_EARPIECE && dev.getType() != AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) {
            retdevs.put(new JSONObject().put("id", dev.getId()).put("type", dev.getType()).put("name",
                dev.getProductName().toString()));
          }
        }
      }

      return new JSONObject().put("devices", retdevs);
    } catch (JSONException e) {
      // lets hope json-object keys are not null and not duplicated :)
    }

    return new JSONObject();
  }

}
