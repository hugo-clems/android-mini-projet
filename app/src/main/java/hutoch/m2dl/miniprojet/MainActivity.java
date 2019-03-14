package hutoch.m2dl.miniprojet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import hutoch.m2dl.miniprojet.utils.DetectNoise;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, SensorEventListener, LocationListener {

    /* *** Constantes *** */
    private static final int DELTA_TOUCH = 5;
    private static final int DELTA_NOISE = 1;
    private static final int DELTA_ACCELERO = 3;
    private static final int DELTA_LUMIN = 5;
    private static final int NOISE_POLL_INTERVAL = 300;
    private static final int PERMISSION_ALL = 1;

    /* *** Utils *** */
    private DetectNoise noiseSensor;
    private PowerManager.WakeLock mWakeLock;    // App needs to have the device stay on
    private Handler noiseHandler = new Handler();
    private SensorManager sensorManager;
    private Sensor acceleroSensor;
    private Sensor luminSensor;
    private LocationManager locationManager;
    private boolean permissionsOK = false;

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
    private Button btnSelected;
    private ProgressBar progressBarNoise;
    private ProgressBar progressBarTouch;
    private ProgressBar progressBarAccelero;
    private ProgressBar progressBarLumin;
    private ProgressBar progressBarGPS;

    /* *** Valeurs *** */
    private ArrayList<Float> touchData;
    private ArrayList<Float> noiseData;
    private ArrayList<Float> acceleroData;
    private ArrayList<Float> luminData;
    private ArrayList<Float> gpsData;
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
    private Location locationOrigin;
    private boolean isLocationOrigin = true;

    /**
     * A la création de l'activité.
     */
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
        btnSelected = findViewById(R.id.btnSelected);
        btnSelected.setEnabled(false);
        progressBarTouch = findViewById(R.id.vertical_progressbar01);
        progressBarNoise = findViewById(R.id.vertical_progressbar02);
        progressBarAccelero = findViewById(R.id.vertical_progressbar03);
        progressBarLumin = findViewById(R.id.vertical_progressbar04);
        progressBarGPS = findViewById(R.id.vertical_progressbar05);

        // Initialisation des élements
        tvTouchAct.setText("Valeur actuelle : 0");
        tvTouchMax.setText("Valeur max : 0");
        tvNoiseAct.setText("Valeur actuelle : 0");
        tvNoiseMax.setText("Valeur max : 0");
        tvAcceleroAct.setText("Valeur actuelle : 0");
        tvAcceleroMax.setText("Valeur max : 0");
        tvLuminAct.setText("Valeur actuelle : 0");
        tvLuminMax.setText("Valeur max : 0");
        tvGpsAct.setText("Valeur actuelle : 0");
        tvGpsMax.setText("Valeur max : 0");

        // Initialisation des valeurs
        touchData = new ArrayList<>();
        noiseData = new ArrayList<>();
        acceleroData = new ArrayList<>();
        luminData = new ArrayList<>();
        gpsData = new ArrayList<>();
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

        if (!hasGPSPermission() || !hasMicroPermission()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ALL);
        } else {
            permissionsOK = true;
            initSensors();
        }
    }

    public boolean hasMicroPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean hasGPSPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint({"InvalidWakeLockTag", "MissingPermission"})
    private void initSensors() {
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
        sensorManager.registerListener(this, acceleroSensor, SensorManager.SENSOR_DELAY_NORMAL);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        // minDistance peut servir de DELTA
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (permissionsOK) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (permissionsOK) {
            if (!noiseRunning) {
                noiseRunning = true;
                startNoiseSensor();
            }
            sensorManager.registerListener(this, acceleroSensor, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, luminSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
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
        progressBarTouch.setProgress(posy.intValue());

        if (Math.abs(posy - touchAct) > DELTA_TOUCH) {
            touchAct = posy;
            tvTouchAct.setText("Valeur actuel : " + touchAct);

            if (posy > touchMax) {
                touchMax = posy;
                tvTouchMax.setText("Valeur max : " + touchMax);
                progressBarTouch.setMax(touchMax.intValue());
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
        updateDisplayNoise(0);
        noiseRunning = false;
    }

    /**
     * Met à jour la vue pour le bruit.
     * @param signalEMA niveau sonore en décibels.
     */
    private void updateDisplayNoise(int signalEMA) {
        progressBarNoise.setProgress(signalEMA);
        if (Math.abs(signalEMA - noiseAct) > DELTA_NOISE) {
            noiseAct = signalEMA;
            tvNoiseAct.setText("Valeur actuel : " + noiseAct +"dB");

            if (noiseAct > noiseMax) {
                noiseMax = noiseAct;
                tvNoiseMax.setText("Valeur max : " + noiseMax + "dB");
                progressBarNoise.setMax(noiseMax);
            }

            noiseData.add(noiseAct + 0f);
        }
    }

    /**
     * Quand un capteur change de valeur.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensor = event.sensor.getType();

        synchronized (this) {
            if (sensor == Sensor.TYPE_LIGHT) {
                Float lightSensor = event.values[0];
                progressBarLumin.setProgress(lightSensor.intValue());

                if(Math.abs(lightSensor - luminAct) > DELTA_LUMIN) {
                    luminAct = lightSensor;
                    tvLuminAct.setText("Valeur actuelle : " + luminAct);

                    if (lightSensor > luminMax) {
                        luminMax = lightSensor;
                        tvLuminMax.setText("Valeur max : " + luminMax);
                        progressBarLumin.setMax(luminMax.intValue());
                    }

                    luminData.add(luminAct);
                }
            } else if (sensor == Sensor.TYPE_ACCELEROMETER) {
                Float accelSensor = Math.abs(event.values[2]);
                progressBarAccelero.setProgress(accelSensor.intValue());

                if (Math.abs(accelSensor - acceleroAct) > DELTA_ACCELERO) {
                    acceleroAct = accelSensor;
                    tvAcceleroAct.setText("Valeur actuel : " + acceleroAct);

                    if (acceleroAct > acceleroMax) {
                        acceleroMax = acceleroAct;
                        tvAcceleroMax.setText("Valeur max : " + acceleroMax);
                        progressBarAccelero.setMax(acceleroMax.intValue());
                    }

                    acceleroData.add(acceleroAct);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Non utilisé
    }

    /**
     * Quand on clic sur une progressbar (permet la sélection du mode de jeu).
     * @param v
     */
    public void clicProgressBar(View v) {
        btnSelected.setTag(v.getTag());
        String tag = "" + v.getTag();
        String text = "";
        switch (tag) {
            case "1":
                text = "Jouer avec Touch";
                break;
            case "2":
                text = "Jouer avec Noise";
                break;
            case "3":
                text = "Jouer avec Accelero";
                break;
            case "4":
                text = "Jouer avec Lumin";
                break;
            case "5":
                text = "Jouer avec GPS";
                break;
        }
        this.btnSelected.setText(text);
        btnSelected.setEnabled(true);
    }

    /**
     * Envois les valeurs au jeu.
     * @param values
     * @param intent
     */
    public void pushValues(ArrayList<Float> values, Intent intent) {
        float[] array = new float[values.size()];
        for(int i = 0; i < values.size(); i++) {
            array[i] = values.get(i);
        }
        intent.putExtra("donnees", array);
    }

    /**
     * Quand on clic sur le bouton pour lancer le jeu.
     * @param v
     */
    public void clicBtnSelected(View v) {
        String tag = "" + v.getTag();
        String titre = "";
        Intent intent = new Intent(this, JeuActivity.class);
        switch (tag) {
            case "1":
                titre = "Jouer avec Touch";
                pushValues(touchData, intent);
                intent.putExtra("valMax", this.touchMax);
                break;
            case "2":
                titre = "Jouer avec Noise";
                pushValues(noiseData, intent);
                intent.putExtra("valMax", this.noiseMax + 0f);
                break;
            case "3":
                titre = "Jouer avec Accelero";
                pushValues(acceleroData, intent);
                intent.putExtra("valMax", this.acceleroMax);
                break;
            case "4":
                titre = "Jouer avec Lumin";
                pushValues(luminData, intent);
                intent.putExtra("valMax", this.luminMax);
                break;
            case "5":
                titre = "Jouer avec GPS";
                pushValues(gpsData, intent);
                intent.putExtra("valMax", this.gpsMax);
                break;
        }
        intent.putExtra("titre", titre);
        startActivity(intent);
    }

    @Override
    public void onLocationChanged(Location location) {
        if(isLocationOrigin) {
            locationOrigin = location;
            isLocationOrigin = false;
        }

        gpsAct = location.distanceTo(locationOrigin);
        progressBarGPS.setProgress(gpsAct.intValue());
        tvGpsAct.setText("Valeur actuelle : " + gpsAct);

        if (gpsAct > gpsMax) {
            gpsMax = gpsAct;
            tvGpsMax.setText("Valeur max : " + gpsMax);
            progressBarGPS.setMax(gpsMax.intValue());
        }

        gpsData.add(gpsAct);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ALL: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    permissionsOK = true;
                    initSensors();
                } else {
                    //
                }
                return;
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}