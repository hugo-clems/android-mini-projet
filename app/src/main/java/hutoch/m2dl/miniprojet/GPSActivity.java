package hutoch.m2dl.miniprojet;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class GPSActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor gps;

    private TextView tvMax;
    private TextView tvAct;
    private List<Float> gpsData;

    private Float gpsMax;
    private Float gpsAct;

    private static final int DELTA = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);

        tvMax = findViewById(R.id.textView5);
        tvAct = findViewById(R.id.textView6);

        tvMax.setText("0");
        tvAct.setText("0");

        gpsData = new ArrayList<>();

        gpsMax = 0f;
        gpsAct = 0f;

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gps = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensor = event.sensor.getType();
        float [] values = event.values;

        synchronized (this) {
            if (sensor == Sensor.TYPE_MAGNETIC_FIELD) {
                Float gpsSensor = Math.abs(values[0]);

                if(Math.abs(gpsSensor - gpsAct) > DELTA) {
                    gpsAct = gpsSensor;
                    tvAct.setText(gpsAct + "");

                    if (gpsSensor > gpsMax) {
                        gpsMax = gpsSensor;
                        tvMax.setText(gpsMax + "");
                    }

                    gpsData.add(gpsAct);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Nothing here
    }

    @Override
    protected void onResume() {
        // Register a listener for the sensor.
        super.onResume();
        sensorManager.registerListener(this, gps, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        // Be sure to unregister the sensor when the activity pauses.
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onStop() {
        sensorManager.unregisterListener(this);
        super.onStop();
    }
}
