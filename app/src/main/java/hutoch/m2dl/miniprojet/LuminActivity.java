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

public class LuminActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor light;

    private TextView tvMax;
    private TextView tvAct;
    private List<Float> lightData;

    private Float lightMax;
    private Float lightAct;

    private static final int DELTA = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lumin);

        tvMax = findViewById(R.id.textView3);
        tvAct = findViewById(R.id.textView4);

        tvMax.setText("0");
        tvAct.setText("0");

        lightData = new ArrayList<>();

        lightMax = 0f;
        lightAct = 0f;

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);


    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensor = event.sensor.getType();
        float [] values = event.values;

        synchronized (this) {
            if (sensor == Sensor.TYPE_LIGHT) {
                Float lightSensor = values[0];

                if(Math.abs(lightSensor - lightAct) > DELTA) {
                    lightAct = lightSensor;
                    tvAct.setText(lightAct + "");

                    if (lightSensor > lightMax) {
                        lightMax = lightSensor;
                        tvMax.setText(lightMax + "");
                    }

                    lightData.add(lightAct);
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
        sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        // Be sure to unregister the sensor when the activity pauses.
        super.onPause();
        sensorManager.unregisterListener(this);
    }

}
