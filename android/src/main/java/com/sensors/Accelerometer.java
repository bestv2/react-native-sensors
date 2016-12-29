package com.sensors;

import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.support.annotation.Nullable;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

public class Accelerometer extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;
  private final SensorManager sensorManager;
  private final Sensor sensor;
  private final int interval;

  public Accelerometer(ReactApplicationContext reactContext) implements SensorEventListener {
    super(reactContext);
    this.reactContext = reactContext;
    this.sensorManager = (SensorManager)reactContext.getSystemService(reactContext.SENSOR_SERVICE);
    this.sensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    if (this.sensor == null) {
      // No sensor found, throw error
      throw new RuntimeException("No Accelerometer found");
    }
  }

  // RN Methods
  @ReactMethod
  public void setUpdateInterval(int newInterval) {
    this.interval = newInterval;
  }

  @ReactMethod
  public void startUpdates() {
    // Milisecond to Mikrosecond conversion
    sensorManager.registerListener(this, sensor, this.interval * 1000);
  }

  @ReactMethod
  public void stopUpdates() {
    sensorManager.unregisterListener(this);
  }

  @Override
  public String getName() {
    return "RNSensors";
  }

  // SensorEventListener Interface
  private void sendEvent(String eventName, @Nullable WritableMap params) {
		try {
			this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
				.emit(eventName, params);
		} catch (RuntimeException e) {
			Log.e("ERROR", "java.lang.RuntimeException: Trying to invoke Javascript before CatalystInstance has been set!");
		}
	}

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
      Sensor mySensor = sensorEvent.sensor;
      WritableMap map = mArguments.createMap();

      if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				map.putDouble("x", sensorEvent.values[0]);
				map.putDouble("y", sensorEvent.values[1]);
				map.putDouble("z", sensorEvent.values[2]);
        map.putDouble("timestamp", (double) curTime);
        sendEvent("Accelerometer", map);
      }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
