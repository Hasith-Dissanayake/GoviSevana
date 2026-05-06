package lk.javainstitute.govisevana.helper;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Window;
import android.view.WindowManager;

public class ProximitySensorHelper implements SensorEventListener {
    private final SensorManager sensorManager;
    private final Sensor proximitySensor;
    private final Activity activity;

    public ProximitySensorHelper(Activity activity) {
        this.activity = activity;
        sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        proximitySensor = (sensorManager != null) ? sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) : null;
    }

    public void registerSensor() {
        if (proximitySensor != null) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void unregisterSensor() {
        if (proximitySensor != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            Window window = activity.getWindow();
            WindowManager.LayoutParams layoutParams = window.getAttributes();

            if (event.values[0] < proximitySensor.getMaximumRange()) {
                layoutParams.screenBrightness = 0.01f; // Dim screen
            } else {
                layoutParams.screenBrightness = 0.1f; // Restore brightness
            }
            window.setAttributes(layoutParams);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}