package com.greensoft.greenplayer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class SubtitleSettingsActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private TextView tvEncoding, tvSize, tvColor;

    // Full global subtitle encodings matching MX Player
    private final String[] encodings = new String[]{
        "UTF-8 (Default)",
        "UTF-16",
        "UTF-16BE",
        "UTF-16LE",
        "US-ASCII",
        "ISO-8859-1 (Western European)",
        "ISO-8859-2 (Central European)",
        "ISO-8859-5 (Cyrillic)",
        "ISO-8859-6 (Arabic)",
        "ISO-8859-7 (Greek)",
        "ISO-8859-8 (Hebrew)",
        "ISO-8859-9 (Turkish)",
        "Windows-1250 (Central European)",
        "Windows-1251 (Cyrillic)",
        "Windows-1252 (Western European / Latin I)",
        "Windows-1253 (Greek)",
        "Windows-1254 (Turkish)",
        "Windows-1255 (Hebrew)",
        "Windows-1256 (Arabic)",
        "Shift_JIS (Japanese)",
        "EUC-JP (Japanese)",
        "EUC-KR (Korean)",
        "GBK / GB18030 (Chinese Simplified)",
        "Big5 (Chinese Traditional)",
        "TIS-620 (Thai)",
        "KOI8-R (Russian)"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_subtitle);

        prefs = getSharedPreferences("green_player_settings", MODE_PRIVATE);

        ImageView btnBack = findViewById(R.id.btnBackSubtitle);
        btnBack.setOnClickListener(v -> finish());

        tvEncoding = findViewById(R.id.tvSubtitleEncodingVal);
        tvSize = findViewById(R.id.tvSubtitleSizeVal);
        tvColor = findViewById(R.id.tvSubtitleColorVal);

        tvEncoding.setText(prefs.getString("subtitle_encoding", "UTF-8 (Default)"));
        tvSize.setText(prefs.getString("subtitle_size", "Medium (24 sp)"));
        tvColor.setText(prefs.getString("subtitle_color", "White with black border"));

        findViewById(R.id.layoutSubtitleEncoding).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Character encoding")
                .setItems(encodings, (dialog, which) -> {
                    String chosen = encodings[which];
                    tvEncoding.setText(chosen);
                    prefs.edit().putString("subtitle_encoding", chosen).apply();
                    Toast.makeText(this, "Encoding: " + chosen, Toast.LENGTH_SHORT).show();
                })
                .show();
        });

        findViewById(R.id.layoutSubtitleSize).setOnClickListener(v -> {
            String[] sizes = new String[]{"Very Small (16 sp)", "Small (20 sp)", "Medium (24 sp - Default)", "Large (28 sp)", "Very Large (34 sp)"};
            new AlertDialog.Builder(this)
                .setTitle("Text size")
                .setItems(sizes, (dialog, which) -> {
                    tvSize.setText(sizes[which]);
                    prefs.edit().putString("subtitle_size", sizes[which]).apply();
                })
                .show();
        });

        findViewById(R.id.layoutSubtitleColor).setOnClickListener(v -> {
            String[] colors = new String[]{"White with black border", "Yellow with black border", "Green neon with dark border", "Cyan with dark border"};
            new AlertDialog.Builder(this)
                .setTitle("Text color")
                .setItems(colors, (dialog, which) -> {
                    tvColor.setText(colors[which]);
                    prefs.edit().putString("subtitle_color", colors[which]).apply();
                })
                .show();
        });
    }
}
