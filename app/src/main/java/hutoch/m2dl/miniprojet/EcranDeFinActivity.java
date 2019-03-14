package hutoch.m2dl.miniprojet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class EcranDeFinActivity extends AppCompatActivity {

    private TextView tvScoreFinal;
    private TextView tvNbTetes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecran_de_fin);

        int score = (int) getIntent().getSerializableExtra("score");
        int nbTetes = (int) getIntent().getSerializableExtra("nbTetes");

        tvScoreFinal = findViewById(R.id.tvScoreFinal);
        tvNbTetes = findViewById(R.id.tvNbTetes);

        tvScoreFinal.setText(score + " têtes sont passées.");
        tvScoreFinal.setText("Vous avez réussi à en avoir " + score + " !");
    }

    /**
     * Retour à l'accueil.
     * @param v
     */
    public void retourAccueil(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
