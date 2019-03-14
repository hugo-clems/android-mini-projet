package hutoch.m2dl.miniprojet;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class TirAuMigeon extends Activity implements SensorEventListener, View.OnTouchListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastUpdate;

    public AnimatedView animatedView = null;
    public ShapeDrawable mDrawable = new ShapeDrawable();
    public static int x;
    public static int y;
    public static int width;
    public static int height;
    public static final int ballSize = 50;
    public static final float speed = 2;

    private ArrayList<Migeon> migeons;
    private Bitmap migeon;

    private Handler mHandler;
    private ArrayList<Float> listeValeurs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jeu);

        // Récupérations des paramétres
        String titre = (String) getIntent().getSerializableExtra("tag");
        ArrayList<Float> datas = (ArrayList<Float>) getIntent().getSerializableExtra("list");
        setTitle(titre);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        migeons = new ArrayList<Migeon>();
        migeon = BitmapFactory.decodeResource(getResources(), R.drawable.migeon);

        listeValeurs = new ArrayList<Float>();
        listeValeurs.add(10f);
        listeValeurs.add(500f);
        listeValeurs.add(250f);
        listeValeurs.add(100f);
        listeValeurs.add(600f);
        listeValeurs.add(40f);
        listeValeurs.add(300f);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        lastUpdate = System.currentTimeMillis();

        mHandler = new Handler();
        mHandler.postDelayed(mUpdateTimeTask, 1000);

        animatedView = new AnimatedView(this);
        animatedView.setOnTouchListener(this);
        setContentView(animatedView);
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            if(listeValeurs.size() > 0) {
                migeons.add(new Migeon(listeValeurs.remove(0), 0));
            }
            // inserez ici ce que vous voulez executer...
            mHandler.postDelayed(this, 2000);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            int xValue = x - (int) (event.values[0] * speed);
            int yValue = y + (int) (event.values[1] * speed);

            if(xValue > 0 && xValue + ballSize < width)
                x = xValue;

            if(yValue > 0 && yValue + ballSize < height)
                y = yValue;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int posX = x + ballSize / 2;
        int posY = y + ballSize / 2;

        for(Migeon m : migeons) {
            if(posX > m.getX() && posX < m.getX() + migeon.getWidth() && posY > m.getY() && posY < m.getY() + migeon.getHeight()) {
                migeons.remove(m);
                return true;
            }
        }
        return true;
    }

    public class AnimatedView extends android.support.v7.widget.AppCompatImageView {

        public AnimatedView(Context context) {
            super(context);

            mDrawable = new ShapeDrawable(new OvalShape());
            mDrawable.getPaint().setColor(Color.GRAY);
            mDrawable.setBounds(x, y, x + ballSize, y + ballSize);

        }

        @Override
        protected void onDraw(Canvas canvas) {
            //Draw
            for (int i = 0; i < migeons.size(); i++)
            {
                canvas.drawBitmap(migeon, migeons.get(i).getX(), migeons.get(i).getY(), null);
                migeons.get(i).tick();
            }

            //Remove
            for (int i = 0; i < migeons.size(); i++)
            {
                if (migeons.get(i).getY() > canvas.getHeight())
                    migeons.remove(i);
            }
            mDrawable.setBounds(x, y, x + ballSize, y + ballSize);
            mDrawable.draw(canvas);
            invalidate();
        }
    }

}