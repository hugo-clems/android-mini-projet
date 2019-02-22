package hutoch.m2dl.miniprojet;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class Accelerometer extends Activity implements SensorEventListener {

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

    private Float accelMax;
    private Float accelAct;

    private TextView tvMax;
    private TextView tvAct;

    private List<Float> AccelData;

    private static final int DELTA = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvAct = findViewById(R.id.textView);
        tvMax = findViewById(R.id.textView2);

        tvMax.setText("0");
        tvAct.setText("0");

        AccelData = new ArrayList<>();

        accelMax = 0f;
        accelAct = 0f;

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float z = Math.abs(event.values[2]);

            if (Math.abs(z - accelAct) > DELTA) {
                accelAct = z;
                tvAct.setText("" + accelAct);

                if (accelAct > accelMax) {
                    accelMax = accelAct;
                    tvMax.setText("" + accelMax);
                }

                AccelData.add(accelAct);
            }
        }
    }

    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
