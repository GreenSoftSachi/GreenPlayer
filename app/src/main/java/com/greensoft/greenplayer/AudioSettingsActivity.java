package com.greensoft.greenplayer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.audiofx.AudioEffect;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class AudioSettingsActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_audio);

        prefs = getSharedPreferences("green_player_settings", MODE_PRIVATE);

        ImageView btnBack = findViewById(R.id.btnBackAudio);
        btnBack.setOnClickListener(v -> finish());

        CheckBox cbAudioPlayerMode = findViewById(R.id.cbAudioPlayerMode);
        CheckBox cbVolumeBoost = findViewById(R.id.cbVolumeBoost);
        TextView tvAudioDelay = findViewById(R.id.tvAudioDelayVal);

        cbAudioPlayerMode.setChecked(prefs.getBoolean("audio_player_mode", true));
        cbVolumeBoost.setChecked(prefs.getBoolean("audio_volume_boost", true));
        tvAudioDelay.setText(prefs.getInt("audio_delay_ms", 0) + " ms");

        cbAudioPlayerMode.setOnCheckedChangeListener((b, isChecked) -> prefs.edit().putBoolean("audio_player_mode", isChecked).apply());
        cbVolumeBoost.setOnCheckedChangeListener((b, isChecked) -> prefs.edit().putBoolean("audio_volume_boost", isChecked).apply());

        findViewById(R.id.layoutAudioDelay).setOnClickListener(v -> {
            String[] delays = new String[]{"-500 ms", "-250 ms", "-100 ms", "0 ms (Default)", "+100 ms", "+250 ms", "+500 ms"};
            int[] delayVals = new int[]{-500, -250, -100, 0, 100, 250, 500};
            new AlertDialog.Builder(this)
                .setTitle("Audio Delay")
                .setItems(delays, (dialog, which) -> {
                    tvAudioDelay.setText(delays[which]);
                    prefs.edit().putInt("audio_delay_ms", delayVals[which]).apply();
                })
                .show();
        });

        findViewById(R.id.layoutEqualizer).setOnClickListener(v -> {
            try {
                Intent eqIntent = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
                if (eqIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(eqIntent);
                } else {
                    Toast.makeText(this, "Standard Equalizer enabled in Player", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Built-in Green Equalizer enabled", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
