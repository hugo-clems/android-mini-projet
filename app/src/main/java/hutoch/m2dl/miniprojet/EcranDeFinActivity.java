package hutoch.m2dl.miniprojet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class EcranDeFinActivity extends AppCompatActivity {

    private TextView tvScoreFinal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecran_de_fin);

        int score = (int) getIntent().getSerializableExtra("score");

        tvScoreFinal = findViewById(R.id.tvScoreFinal);
        tvScoreFinal.setText("Score final : " + score);
    }

    public void retourAccueil(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
