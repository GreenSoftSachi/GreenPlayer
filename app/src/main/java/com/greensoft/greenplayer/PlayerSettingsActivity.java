package com.greensoft.greenplayer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class PlayerSettingsActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_player);

        prefs = getSharedPreferences("green_player_settings", MODE_PRIVATE);

        ImageView btnBack = findViewById(R.id.btnBackPlayerSettings);
        btnBack.setOnClickListener(v -> finish());

        CheckBox cbResumeFirstOnly = findViewById(R.id.cbResumeFirstOnly);
        CheckBox cbRememberSelections = findViewById(R.id.cbRememberSelections);
        CheckBox cbBackToList = findViewById(R.id.cbBackToList);
        CheckBox cbPreviewSeeking = findViewById(R.id.cbPreviewSeeking);
        CheckBox cbPreviewSeekingNetwork = findViewById(R.id.cbPreviewSeekingNetwork);
        CheckBox cbFastSeeking = findViewById(R.id.cbFastSeeking);

        cbResumeFirstOnly.setChecked(prefs.getBoolean("player_resume_first_only", false));
        cbRememberSelections.setChecked(prefs.getBoolean("player_remember_selections", true));
        cbBackToList.setChecked(prefs.getBoolean("player_back_to_list", false));
        cbPreviewSeeking.setChecked(prefs.getBoolean("player_preview_seeking", true));
        cbPreviewSeekingNetwork.setChecked(prefs.getBoolean("player_preview_network", false));
        cbFastSeeking.setChecked(prefs.getBoolean("player_fast_seeking", true));

        cbResumeFirstOnly.setOnCheckedChangeListener((b, isChecked) -> prefs.edit().putBoolean("player_resume_first_only", isChecked).apply());
        cbRememberSelections.setOnCheckedChangeListener((b, isChecked) -> prefs.edit().putBoolean("player_remember_selections", isChecked).apply());
        cbBackToList.setOnCheckedChangeListener((b, isChecked) -> prefs.edit().putBoolean("player_back_to_list", isChecked).apply());
        cbPreviewSeeking.setOnCheckedChangeListener((b, isChecked) -> prefs.edit().putBoolean("player_preview_seeking", isChecked).apply());
        cbPreviewSeekingNetwork.setOnCheckedChangeListener((b, isChecked) -> prefs.edit().putBoolean("player_preview_network", isChecked).apply());
        cbFastSeeking.setOnCheckedChangeListener((b, isChecked) -> prefs.edit().putBoolean("player_fast_seeking", isChecked).apply());

        findViewById(R.id.layoutResume).setOnClickListener(v -> {
            String[] options = new String[]{"Ask at startup (Default)", "Always resume", "Never resume (Start from beginning)"};
            new AlertDialog.Builder(this)
                .setTitle("Resume")
                .setItems(options, (dialog, which) -> {
                    prefs.edit().putInt("resume_behavior", which).apply();
                    Toast.makeText(this, "Set: " + options[which], Toast.LENGTH_SHORT).show();
                })
                .show();
        });

        TextView tvDefaultSpeed = findViewById(R.id.tvDefaultSpeedValue);
        findViewById(R.id.layoutDefaultSpeed).setOnClickListener(v -> {
            String[] speeds = new String[]{"0.5x", "0.75x", "1.0x (Normal)", "1.25x", "1.5x", "2.0x"};
            new AlertDialog.Builder(this)
                .setTitle("Default playback speed")
                .setItems(speeds, (dialog, which) -> {
                    tvDefaultSpeed.setText(speeds[which]);
                    prefs.edit().putString("default_playback_speed", speeds[which]).apply();
                })
                .show();
        });
    }
}
