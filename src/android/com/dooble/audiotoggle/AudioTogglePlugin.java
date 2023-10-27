package com.dooble.audiotoggle;

import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.media.AudioManager;
import android.media.AudioDeviceInfo;
import java.util.List;
import android.os.Build;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;

public class AudioTogglePlugin extends CordovaPlugin {
  private static String TAG = "AudioTogglePlugin";
  public static final String ACTION_SET_AUDIO_MODE = "setAudioMode";
  public static final String ACTION_SET_BLUETOOTH_ON = "setBluetoothScoOn";
  public static final String ACTION_SET_SPEAKER_ON = "setSpeakerphoneOn";
  public static final String ACTION_GET_OUTPUT_DEVICES = "getOutputDevices";
  public static final String ACTION_GET_AUDIO_MODE = "getAudioMode";
  public static final String ACTION_IS_SPEAKER_ON = "isSpeakerphoneOn";
  public static final String ACTION_IS_BLUETOOTH_ON = "isBluetoothScoOn";
  public static final String ACTION_HAS_EARPIECE = "hasBuiltInEarpiece";
  public static final String ACTION_HAS_SPEAKER = "hasBuiltInSpeaker";

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    if (action.equals(ACTION_SET_AUDIO_MODE)) {
      this.cordova.getThreadPool().execute(new Runnable() {
          @Override
          public void run() {
            try {
              String mode = args.getString(0);
              if (!setAudioMode(mode)) {
                  callbackContext.error("Invalid audio mode");
              } else {
                  callbackContext.success(mode);
              }
            } catch (JSONException e) {
              callbackContext.error("Invalid parameter was sent to 'setAudioMode'");
            }
          }
      });
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
    } else if (action.equals(ACTION_GET_AUDIO_MODE)) {
      callbackContext.success(getAudioMode());
      return true;
    } else if (action.equals(ACTION_IS_SPEAKER_ON)) {
      callbackContext.success(isSpeakerphoneOn().toString());
      return true;
    } else if (action.equals(ACTION_IS_BLUETOOTH_ON)) {
      callbackContext.success(isBluetoothScoOn().toString());
      return true;
    } else if (action.equals(ACTION_HAS_EARPIECE)) {
      callbackContext.success(hasBuiltInEarpiece().toString());
      return true;
    } else if (action.equals(ACTION_HAS_SPEAKER)) {
      callbackContext.success(hasBuiltInSpeaker().toString());
      return true;
    }

    callbackContext.error("Invalid action");
    return false;
  }

  public Boolean hasBuiltInEarpiece() {
    final Context context = webView.getContext();
    final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

    try {
      AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);

      JSONArray retdevs = new JSONArray();
      for (AudioDeviceInfo dev : devices) {
        if (dev.isSink()) {
          if (dev.getType() == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE) {
            return true;
          }
        }
      }

      return false;
    } catch (Exception e) {
      return false;
    }
  }

  public Boolean hasBuiltInSpeaker() {
    final Context context = webView.getContext();
    final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

    try {
      AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);

      JSONArray retdevs = new JSONArray();
      for (AudioDeviceInfo dev : devices) {
        if (dev.isSink()) {
          if (dev.getType() == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) {
            return true;
          }
        }
      }

      return false;
    } catch (Exception e) {
      return false;
    }
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
    Log.d(TAG, "setAudioMode called: " + mode);
    final Context context = webView.getContext();
    final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    AudioDeviceInfo speakerDevice = null;
    AudioDeviceInfo bluetoothDevice = null;
    List<AudioDeviceInfo>  devices = audioManager.getAvailableCommunicationDevices();
    for (AudioDeviceInfo device : devices) {
        if (device.getType() == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) {
            speakerDevice = device;
            break;
        }
    }
    for (AudioDeviceInfo device : devices) {
        if ((device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_SCO) || (device.getType() == AudioDeviceInfo.TYPE_TELEPHONY)) {
            bluetoothDevice = device;
            break;
        }
    }

    if (mode.equals("bluetooth")) {
      audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
      audioManager.setBluetoothScoOn(true);
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        audioManager.clearCommunicationDevice();
        if (bluetoothDevice != null) {
          boolean result2 = audioManager.setCommunicationDevice(bluetoothDevice);
          if (!result2) {
              // Handle error.
              Log.d(TAG, "setAudioMode bluetooth failed");
          }
        }
      } else {
        audioManager.startBluetoothSco();
        audioManager.setSpeakerphoneOn(false);
      }
      return true;
    } else if (mode.equals("earpiece")) {
      audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
      audioManager.setBluetoothScoOn(false);
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        audioManager.clearCommunicationDevice();
        Log.d(TAG, "setAudioMode cleared CommunicationDevice ");
      } else {
        audioManager.stopBluetoothSco();
        audioManager.setSpeakerphoneOn(false);
      }
      return true;
    } else if (mode.equals("speaker")) {
      audioManager.setMode(AudioManager.MODE_NORMAL);
      audioManager.clearCommunicationDevice();
      audioManager.setBluetoothScoOn(false);
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        if (speakerDevice != null) {
            // Turn speakerphone ON.
            boolean result = audioManager.setCommunicationDevice(speakerDevice);
            if (!result) {
                // Handle error.
                Log.d(TAG, "setAudioMode setCommunicationDevice to speaker failed");
            }
        }
      } else {
        audioManager.stopBluetoothSco();
        audioManager.setSpeakerphoneOn(true);
      }
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
          if (dev.getType() != AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
              && dev.getType() != AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) {
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

  public String getAudioMode() {
    final Context context = webView.getContext();
    final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

    int mode = audioManager.getMode();
    boolean isBluetoothScoOn = audioManager.isBluetoothScoOn();
    boolean isSpeakerphoneOn = audioManager.isSpeakerphoneOn();

    if (mode == AudioManager.MODE_IN_COMMUNICATION && isBluetoothScoOn) {
      return "bluetooth";
    } else if (mode == AudioManager.MODE_IN_COMMUNICATION && !isBluetoothScoOn && !isSpeakerphoneOn) {
      return "earpiece";
    } else if (mode == AudioManager.MODE_IN_COMMUNICATION && !isBluetoothScoOn && isSpeakerphoneOn) {
      return "speaker";
    } else if (mode == AudioManager.MODE_RINGTONE && !isSpeakerphoneOn) {
      return "ringtone";
    } else if (mode == AudioManager.MODE_NORMAL && !isSpeakerphoneOn) {
      return "normal";
    }

    return "normal";
  }

  public Boolean isBluetoothScoOn() {
    final Context context = webView.getContext();
    final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

    return audioManager.isBluetoothScoOn();
  }

  public Boolean isSpeakerphoneOn() {
    final Context context = webView.getContext();
    final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

    return audioManager.isSpeakerphoneOn();
  }
}
