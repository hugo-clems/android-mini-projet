package hutoch.m2dl.miniprojet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import hutoch.m2dl.miniprojet.utils.DetectNoise;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, SensorEventListener {

    /* *** Constantes *** */
    private static final int DELTA_TOUCH = 5;
    private static final int DELTA_NOISE = 1;
    private static final int DELTA_ACCELERO = 3;
    private static final int DELTA_LUMIN = 5;
    private static final int DELTA_GPS = 1;
    private static final int NOISE_POLL_INTERVAL = 300;
    private static final int RECORD_AUDIO = 0;

    /* *** Utils *** */
    private DetectNoise noiseSensor;
    private PowerManager.WakeLock mWakeLock;    // App needs to have the device stay on
    private Handler noiseHandler = new Handler();
    private SensorManager sensorManager;
    private Sensor acceleroSensor;
    private Sensor luminSensor;
    private Sensor gpsSensor;

    /* *** Elements de la vue *** */
    private LinearLayout linearLayout;
    private TextView tvTouchAct;
    private TextView tvTouchMax;
    private TextView tvNoiseAct;
    private TextView tvNoiseMax;
    private TextView tvAcceleroAct;
    private TextView tvAcceleroMax;
    private TextView tvLuminAct;
    private TextView tvLuminMax;
    private TextView tvGpsAct;
    private TextView tvGpsMax;
    private ProgressBar pbNoise;

    /* *** Valeurs *** */
    private List<Float> touchData;
    private List<Float> acceleroData;
    private List<Float> luminData;
    private List<Float> gpdData;
    private Float touchMax;
    private Float touchAct;
    private Float acceleroAct;
    private Float acceleroMax;
    private int noiseAct;
    private int noiseMax;
    private Float luminAct;
    private Float luminMax;
    private Float gpsAct;
    private Float gpsMax;
    private boolean noiseRunning = false;


    /**
     * A la création de l'activité.
     */
    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get elements
        linearLayout = findViewById(R.id.linearLayout);
        tvTouchAct = findViewById(R.id.tvTouchAct);
        tvTouchMax = findViewById(R.id.tvTouchMax);
        tvNoiseAct = findViewById(R.id.tvNoiseAct);
        tvNoiseMax = findViewById(R.id.tvNoiseMax);
        tvAcceleroAct = findViewById(R.id.tvAcceleroAct);
        tvAcceleroMax = findViewById(R.id.tvAcceleroMax);
        tvLuminAct = findViewById(R.id.tvLuminAct);
        tvLuminMax = findViewById(R.id.tvLuminMax);
        tvGpsAct = findViewById(R.id.tvGpsAct);
        tvGpsMax = findViewById(R.id.tvGpsMax);
        pbNoise = findViewById(R.id.pbNoise);

        // Initialisation des élements
        tvTouchAct.setText("0");
        tvTouchMax.setText("0");
        tvNoiseAct.setText("0");
        tvNoiseMax.setText("0");
        tvAcceleroAct.setText("0");
        tvAcceleroMax.setText("0");
        tvLuminAct.setText("0");
        tvLuminMax.setText("0");
        tvGpsAct.setText("0");
        tvGpsMax.setText("0");

        // Initialisation des valeurs
        touchData = new ArrayList<>();
        acceleroData = new ArrayList<>();
        luminData = new ArrayList<>();
        gpdData = new ArrayList<>();
        touchAct = 0f;
        touchMax = 0f;
        acceleroAct = 0f;
        acceleroMax = 0f;
        noiseAct = 0;
        noiseMax = 0;
        luminAct = 0f;
        luminMax = 0f;
        gpsAct = 0f;
        gpsMax = 0f;

        // Touch event
        linearLayout.setOnTouchListener(this);

        // Noise event
        noiseSensor = new DetectNoise();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "NoiseAlert");

        // Accelero event
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        acceleroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        luminSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        gpsSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, acceleroSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!noiseRunning) {
            noiseRunning = true;
            startNoiseSensor();
        }
        sensorManager.registerListener(this, acceleroSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, luminSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gpsSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopNoiseSensor();
        sensorManager.unregisterListener(this);
    }

    /**
     * Quand on touche l'écran.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Float posy = event.getY();

        if (Math.abs(posy - touchAct) > DELTA_TOUCH) {
            touchAct = posy;
            tvTouchAct.setText("Valeur actuel : " + touchAct);

            if (posy > touchMax) {
                touchMax = posy;
                tvTouchMax.setText("Valeur max : " + touchMax);
            }

            touchData.add(touchAct);
        }

        return true;
    }

    private Runnable noiseSleepTask = new Runnable() {
        public void run() {
            startNoiseSensor();
        }
    };

    private Runnable noisePollTask = new Runnable() {
        public void run() {
            updateDisplayNoise(noiseSensor.getAmplitude());
            noiseHandler.postDelayed(noisePollTask, NOISE_POLL_INTERVAL);
        }
    };

    /**
     * Démarre le détecteur de bruits.
     */
    private void startNoiseSensor() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO);
        }

        noiseSensor.start();
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }

        noiseHandler.postDelayed(noisePollTask, NOISE_POLL_INTERVAL);
    }

    /**
     * Arrête le détecteur de bruits.
     */
    private void stopNoiseSensor() {
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        noiseHandler.removeCallbacks(noiseSleepTask);
        noiseHandler.removeCallbacks(noisePollTask);
        noiseSensor.stop();
        pbNoise.setProgress(0);
        updateDisplayNoise(0);
        noiseRunning = false;
    }

    /**
     * Met à jour la vue pour le bruit.
     * @param signalEMA niveau sonore en décibels.
     */
    private void updateDisplayNoise(int signalEMA) {
        if (Math.abs(signalEMA - noiseAct) > DELTA_NOISE) {
            noiseAct = signalEMA;
            pbNoise.setProgress(noiseAct);
            tvNoiseAct.setText("Valeur actuel : " + noiseAct +"dB");

            if (noiseAct > noiseMax) {
                noiseMax = noiseAct;
                tvNoiseMax.setText("Valeur max : " + noiseMax + "dB");
            }
        }
    }

    /**
     * Quand un capteur change de valeur.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;
        int sensor = event.sensor.getType();

        synchronized (this) {
            if (sensor == Sensor.TYPE_LIGHT) {
                Float lightSensor = event.values[0];

                if(Math.abs(lightSensor - luminAct) > DELTA_LUMIN) {
                    luminAct = lightSensor;
                    tvLuminAct.setText("Valeur actuelle : " + luminAct);

                    if (lightSensor > luminMax) {
                        luminMax = lightSensor;
                        tvLuminMax.setText("Valeur max : " + luminMax);
                    }

                    luminData.add(luminAct);
                }
            } else if (sensor == Sensor.TYPE_MAGNETIC_FIELD) {
                Float gpsSensor = Math.abs(event.values[0]);

                if (Math.abs(gpsSensor - gpsAct) > DELTA_GPS) {
                    gpsAct = gpsSensor;
                    tvGpsAct.setText("Valeur actuelle : " +gpsAct);

                    if (gpsSensor > gpsMax) {
                        gpsMax = gpsSensor;
                        tvGpsMax.setText("Valeur max : " + gpsMax);
                    }

                    gpdData.add(gpsAct);
                }
            }
        }

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float z = Math.abs(event.values[2]);

            if (Math.abs(z - acceleroAct) > DELTA_ACCELERO) {
                acceleroAct = z;
                tvAcceleroAct.setText("Valeur actuel : " + acceleroAct);

                if (acceleroAct > acceleroMax) {
                    acceleroMax = acceleroAct;
                    tvAcceleroMax.setText("Valeur max : " + acceleroMax);
                }

                acceleroData.add(acceleroAct);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Non utilisé
    }

}
