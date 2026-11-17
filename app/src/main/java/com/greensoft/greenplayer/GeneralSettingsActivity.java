package com.greensoft.greenplayer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class GeneralSettingsActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private TextView tvLanguage;

    private final String[] languages = new String[]{
        System Default (English),
        සිංහල (Sinhala),
        Español (Spanish),
        Français (French),
        Deutsch (German),
        हिन्दी (Hindi),
        日本語 (Japanese),
        한국어 (Korean),
        中文 (Chinese)
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_general);

        prefs = getSharedPreferences(green_player_settings, MODE_PRIVATE);

        ImageView btnBack = findViewById(R.id.btnBackGeneral);
        btnBack.setOnClickListener(v -> finish());

        tvLanguage = findViewById(R.id.tvAppLanguageVal);
        tvLanguage.setText(prefs.getString(app_language, System Default (English)));

        findViewById(R.id.layoutAppLanguage).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle(App language)
                .setItems(languages, (dialog, which) -> {
                    String chosen = languages[which];
                    tvLanguage.setText(chosen);
                    prefs.edit().putString(app_language, chosen).apply();
                    Toast.makeText(this, Language:  + chosen, Toast.LENGTH_SHORT).show();
                })
                .show();
        });

        findViewById(R.id.layoutClearHistory).setOnClickListener(v -> {
            getSharedPreferences(player_resume_prefs, MODE_PRIVATE).edit().clear().apply();
            Toast.makeText(this, Playback history cleared, Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.layoutClearCache).setOnClickListener(v -> {
            try {
                if (getCacheDir() != null) {
                    for (java.io.File file : getCacheDir().listFiles()) {
                        file.delete();
                    }
                }
                Toast.makeText(this, Thumbnail cache cleared, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, Cache cleared, Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.layoutResetSettings).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle(Reset settings)
                .setMessage(Are you sure you want to restore all settings to default?)
                .setPositiveButton(Reset, (dialog, which) -> {
                    prefs.edit().clear().apply();
                    Toast.makeText(this, Settings restored to defaults, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(Cancel, null)
                .show();
        });
    }
}