package hutoch.m2dl.miniprojet;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class JeuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jeu);
        String str = (String) getIntent().getSerializableExtra("tag");
        TextView tv = findViewById(R.id.tvJeu);
        tv.setText(str);
    }
}