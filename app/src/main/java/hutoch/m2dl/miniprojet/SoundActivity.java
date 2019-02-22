package hutoch.m2dl.miniprojet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SoundActivity extends AppCompatActivity {

    private static final int POLL_INTERVAL = 300;
    private static final int DELTA = 1;

    private boolean mRunning = false;
    int RECORD_AUDIO = 0;
    private PowerManager.WakeLock mWakeLock;
    private Handler mHandler = new Handler();
    private TextView tv_noice, tvSoundMax;
    private DetectNoise mSensor;
    ProgressBar bar;
    int noice, max;

    private Runnable mSleepTask = new Runnable() {
        public void run() {
            start();
        }
    };

    private Runnable mPollTask = new Runnable() {
        public void run() {
            updateDisplay(mSensor.getAmplitude());
            mHandler.postDelayed(mPollTask, POLL_INTERVAL);
        }
    };

    @SuppressLint("InvalidWakeLockTag")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound);
        tv_noice = findViewById(R.id.tv_noice);
        tvSoundMax = findViewById(R.id.tvSoundMax);
        bar = findViewById(R.id.progressBar1);
        mSensor = new DetectNoise();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "NoiseAlert");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mRunning) {
            mRunning = true;
            start();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stop();
    }

    private void start() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO);
        }

        mSensor.start();
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }
        mHandler.postDelayed(mPollTask, POLL_INTERVAL);
    }

    private void stop() {
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        mHandler.removeCallbacks(mSleepTask);
        mHandler.removeCallbacks(mPollTask);
        mSensor.stop();
        bar.setProgress(0);
        updateDisplay(0);
        mRunning = false;
    }

    private void updateDisplay(int signalEMA) {
        if (Math.abs(signalEMA - noice) > DELTA) {
            bar.setProgress(signalEMA);
            tv_noice.setText(signalEMA+"dB");
            noice = signalEMA;
            if (noice > max) {
                max = noice;
                tvSoundMax.setText("Max:"+max+"dB");
            }
        }
    }

}
