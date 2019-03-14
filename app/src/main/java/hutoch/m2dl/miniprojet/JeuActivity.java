package hutoch.m2dl.miniprojet;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;

public class JeuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jeu);
        String titre = (String) getIntent().getSerializableExtra("tag");
        ArrayList<Float> datas = (ArrayList<Float>) getIntent().getSerializableExtra("list");
        setTitle(titre);
    }
}
