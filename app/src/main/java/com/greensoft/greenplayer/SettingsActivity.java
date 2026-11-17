package com.greensoft.greenplayer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ImageView btnBack = findViewById(R.id.btnBackMainSettings);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        findViewById(R.id.menuSettingList).setOnClickListener(v -> 
            startActivity(new Intent(this, ListSettingsActivity.class)));

        findViewById(R.id.menuSettingPlayer).setOnClickListener(v -> 
            startActivity(new Intent(this, PlayerSettingsActivity.class)));

        findViewById(R.id.menuSettingDecoder).setOnClickListener(v -> 
            startActivity(new Intent(this, DecoderSettingsActivity.class)));

        findViewById(R.id.menuSettingAudio).setOnClickListener(v -> 
            startActivity(new Intent(this, AudioSettingsActivity.class)));

        findViewById(R.id.menuSettingSubtitle).setOnClickListener(v -> 
            startActivity(new Intent(this, SubtitleSettingsActivity.class)));

        findViewById(R.id.menuSettingGeneral).setOnClickListener(v -> 
            startActivity(new Intent(this, GeneralSettingsActivity.class)));
    }
}