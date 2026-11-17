package com.greensoft.greenplayer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;

public class CustomPopupPlayActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_popup);

        prefs = getSharedPreferences("green_player_settings", MODE_PRIVATE);

        ImageView btnBack = findViewById(R.id.btnBackPopup);
        btnBack.setOnClickListener(v -> finish());

        RadioGroup rg = findViewById(R.id.rgPopupControls);
        RadioButton rbPrevNext = findViewById(R.id.rbPrevNext);
        RadioButton rbFfRw = findViewById(R.id.rbFastForwardRewind);

        boolean isFfRw = prefs.getBoolean("popup_ff_rw", false);
        if (isFfRw) {
            rbFfRw.setChecked(true);
        } else {
            rbPrevNext.setChecked(true);
        }

        rg.setOnCheckedChangeListener((group, checkedId) -> {
            boolean ffRw = (checkedId == R.id.rbFastForwardRewind);
            prefs.edit().putBoolean("popup_ff_rw", ffRw).apply();
        });
    }
}
