package com.greensoft.greenplayer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class AppThemeActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    // 30 Classic theme colors matching MX Player palette
    private final int[] themeColors = new int[]{
        0xFFF44336, 0xFFFF9800, 0xFFFFC107, 0xFF795548, 0xFFCDDC39, 0xFF8BC34A,
        0xFF4CAF50, 0xFF009688, 0xFF00BCD4, 0xFF2196F3, 0xFF3F51B5, 0xFF9C27B0,
        0xFFE91E63, 0xFFFF4081, 0xFFE040FB, 0xFFECEFF1, 0xFFD32F2F, 0xFFF57C00,
        0xFFFFA000, 0xFF5D4037, 0xFFAFB42B, 0xFF689F38, 0xFF388E3C, 0xFF00796B,
        0xFF0097A7, 0xFF1976D2, 0xFF303F9F, 0xFF7B1FA2, 0xFFC2185B, 0xFFC51162
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_theme);

        prefs = getSharedPreferences("green_player_settings", MODE_PRIVATE);

        ImageView btnBack = findViewById(R.id.btnBackTheme);
        btnBack.setOnClickListener(v -> finish());

        CardView cardAdaptive = findViewById(R.id.cardThemeAdaptive);
        CardView cardLight = findViewById(R.id.cardThemeLight);
        CardView cardDark = findViewById(R.id.cardThemeDark);

        cardAdaptive.setOnClickListener(v -> selectThemeMode("adaptive"));
        cardLight.setOnClickListener(v -> selectThemeMode("light"));
        cardDark.setOnClickListener(v -> selectThemeMode("dark"));

        setupClassicGrid();
    }

    private void selectThemeMode(String mode) {
        prefs.edit().putString("theme_mode", mode).apply();
        Toast.makeText(this, "Theme set to: " + mode.toUpperCase(), Toast.LENGTH_SHORT).show();
    }

    private void setupClassicGrid() {
        GridLayout grid = findViewById(R.id.gridThemes);
        int size = (int) (48 * getResources().getDisplayMetrics().density);
        int margin = (int) (5 * getResources().getDisplayMetrics().density);

        for (int color : themeColors) {
            CardView card = new CardView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = size;
            params.height = size;
            params.setMargins(margin, margin, margin, margin);
            card.setLayoutParams(params);
            card.setRadius(12 * getResources().getDisplayMetrics().density);
            card.setCardElevation(2);
            card.setCardBackgroundColor(color);
            card.setClickable(true);
            card.setFocusable(true);

            card.setOnClickListener(v -> {
                prefs.edit().putInt("theme_color", color).apply();
                Toast.makeText(this, "Color theme applied", Toast.LENGTH_SHORT).show();
            });

            grid.addView(card);
        }
    }
}
