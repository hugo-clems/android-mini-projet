package hutoch.m2dl.miniprojet;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    private TextView tvMax;
    private TextView tvAct;
    private LinearLayout linearLayout;
    private List<Float> touchData;

    private Float touchMax;
    private Float touchAct;

    private static final int DELTA = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linearLayout = findViewById(R.id.linearLayout);
        tvAct = findViewById(R.id.textView);
        tvMax = findViewById(R.id.textView2);

        tvMax.setText("0");
        tvAct.setText("0");

        touchData = new ArrayList<>();

        touchMax = 0f;
        touchAct = 0f;


        linearLayout.setOnTouchListener(this);

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Float posy = event.getY();

        if (Math.abs(posy - touchAct) > DELTA) {
            touchAct = posy;
            tvAct.setText("" + touchAct);

            if (posy > touchMax) {
                touchMax = posy;
                tvMax.setText("" + touchMax);
            }

            touchData.add(touchAct);
        }


        return true;
    }
}
