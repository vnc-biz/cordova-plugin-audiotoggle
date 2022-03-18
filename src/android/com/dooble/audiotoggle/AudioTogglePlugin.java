package com.dooble.audiotoggle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.Context;
import android.media.AudioManager;
import android.media.AudioDeviceInfo;
import java.util.List;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.Manifest;
import android.util.Log;
import android.view.KeyEvent;

public class AudioTogglePlugin extends CordovaPlugin {
  public static final String ACTION_SET_AUDIO_MODE = "setAudioMode";
  public static final String ACTION_SET_BLUETOOTH_ON = "setBluetoothScoOn";
  public static final String ACTION_SET_SPEAKER_ON = "setSpeakerphoneOn";
  public static final String ACTION_GET_OUTPUT_DEVICES = "getOutputDevices";
  public static final String ACTION_GET_AUDIO_MODE = "getAudioMode";
  public static final String ACTION_IS_SPEAKER_ON = "isSpeakerphoneOn";
  public static final String ACTION_IS_BLUETOOTH_ON = "isBluetoothScoOn";
  public static final String ACTION_HAS_EARPIECE = "hasBuiltInEarpiece";
  public static final String ACTION_HAS_SPEAKER = "hasBuiltInSpeaker";
  public static final String ACTION_SET_MODE_IN_CALL = "setModeInCall";
  public static final String ACTION_REGISTER_BLUETOOTH_EVENTS = "registerBluetoothEvent";
  private static String TAG = "AudioTogglePlugin";

  protected  Context mContext;

  private BluetoothAdapter mBluetoothAdapter;
  private BluetoothHeadset mBluetoothHeadset;
  private BluetoothDevice mConnectedHeadset;
  private boolean mIsStarted = false;

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
      super.initialize(cordova, webView);
      mContext            = cordova.getActivity();
      if(cordova.hasPermission(Manifest.permission.BLUETOOTH)) {
        this.registerBluetoothEvent();
      }
  }

  private Boolean registerBluetoothEvent() {
    if (!mIsStarted) {
        mBluetoothAdapter   = BluetoothAdapter.getDefaultAdapter();
        mIsStarted = mBluetoothAdapter.getProfileProxy(mContext, mHeadsetProfileListener, BluetoothProfile.HEADSET);
    }
    return mIsStarted;
  }
  private BluetoothProfile.ServiceListener mHeadsetProfileListener = new BluetoothProfile.ServiceListener()
  {
      @Override
      public void onServiceDisconnected(int profile)
      {
          Log.d(TAG, "Profile listener onServiceDisconnected"); //$NON-NLS-1$
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public void onServiceConnected(int profile, BluetoothProfile proxy)
      {
          Log.d(TAG, "Profile listener onServiceConnected"); //$NON-NLS-1$

          // mBluetoothHeadset is just a headset profile, does not represent a headset device.
          mBluetoothHeadset = (BluetoothHeadset) proxy;

          // If a headset is connected before this application starts, ACTION_CONNECTION_STATE_CHANGED will not be broadcast.
          // ==> So we need to check for already connected headset.
          List<BluetoothDevice> devices = mBluetoothHeadset.getConnectedDevices();
          if (devices.size() > 0) {
              // Only one headset can be connected at a time, so the connected headset is at index 0.
              mConnectedHeadset       = devices.get(0);
              Log.d(TAG, "connected headset: " + mConnectedHeadset.getName()); //$NON-NLS-1$
          }  else {
              mConnectedHeadset       = null;
          }

          // During the active life time of the app, a user may turn on and off the headset.
          // => register for broadcast of connection states.
          // IntentFilter filter = new IntentFilter();
          // filter.addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);
          // filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
          // filter.addAction(BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT);
          mContext.registerReceiver(mHeadsetBroadcastReceiver, new IntentFilter(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED));
          mContext.registerReceiver(mHeadsetBroadcastReceiver, new IntentFilter(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED));
          mContext.registerReceiver(mHeadsetBroadcastReceiver, new IntentFilter(BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT));
          mContext.registerReceiver(mHeadsetBroadcastReceiver, new IntentFilter(Intent.ACTION_MEDIA_BUTTON));
      }
  };
  private BroadcastReceiver mHeadsetBroadcastReceiver = new BroadcastReceiver()
  {
      @SuppressWarnings("synthetic-access")
      @Override
      public void onReceive(Context context, Intent intent) {
          String action = intent.getAction();
          int state;
          Log.d(TAG, "mHeadsetBroadcastReceiver: action=" + action);
          if (action.equals(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)) {
              state = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_DISCONNECTED);
              Log.d(TAG, "\nAction = " + action + "\nState = " + state); //$NON-NLS-1$ //$NON-NLS-2$
              if (state == BluetoothHeadset.STATE_CONNECTED) {
                  mConnectedHeadset       = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                  Log.d(TAG, "connected headset: " + mConnectedHeadset.getName()); //$NON-NLS-1$
                  Log.d(TAG, "connected headset: " + mConnectedHeadset.getName()); //$NON-NLS-1$
              } else if (state == BluetoothHeadset.STATE_DISCONNECTED) {
                  Log.d(TAG, "disconnected Headset " + mConnectedHeadset.getName()); //$NON-NLS-1$
                  mConnectedHeadset       = null;
              }
          } else if (action.equals(Intent.ACTION_MEDIA_BUTTON)) {
            final KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            Log.d(TAG, "\nAction ACTION_MEDIA_BUTTON = " + action + "\nEvent = " + event);
            if (event != null) {
              final int keycode = event.getKeyCode();
              final int action2 = event.getAction();
              Log.d(TAG, "\nkeycode = " + keycode + "\naction = " + action2);
            }
          } else {
              state = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_AUDIO_DISCONNECTED);
              Log.d(TAG, "\nAction case 2 = " + action + "\nState = " + state); //$NON-NLS-1$ //$NON-NLS-2$
              if (state == BluetoothHeadset.STATE_AUDIO_CONNECTED) {
                  Log.d(TAG, "\nHeadset SCOAUDIO connected");  //$NON-NLS-1$
              } else if (state == BluetoothHeadset.STATE_AUDIO_DISCONNECTED) {
                  Log.d(TAG, "Headset SCOAUDIO disconnected"); //$NON-NLS-1$
              }
          }
      }
  };


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
    } else if (action.equals(ACTION_REGISTER_BLUETOOTH_EVENTS)) {
      callbackContext.success(registerBluetoothEvent().toString());
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
    final Context context = webView.getContext();
    final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

    if (mode.equals("bluetooth")) {
      audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
      audioManager.setBluetoothScoOn(true);
      audioManager.startBluetoothSco();
      audioManager.setSpeakerphoneOn(false);
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
