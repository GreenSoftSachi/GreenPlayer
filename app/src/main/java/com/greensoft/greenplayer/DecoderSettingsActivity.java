package com.greensoft.greenplayer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DecoderSettingsActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private TextView tvCustomCodecPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_decoder);

        prefs = getSharedPreferences(green_player_settings, MODE_PRIVATE);

        ImageView btnBack = findViewById(R.id.btnBackDecoder);
        btnBack.setOnClickListener(v -> finish());

        CheckBox cbHwPlusVideo = findViewById(R.id.cbHwPlusVideo);
        CheckBox cbHwPlusAudio = findViewById(R.id.cbHwPlusAudio);
        CheckBox cbSwAudio = findViewById(R.id.cbSwAudio);
        tvCustomCodecPath = findViewById(R.id.tvCustomCodecPath);

        cbHwPlusVideo.setChecked(prefs.getBoolean(hw_plus_video, true));
        cbHwPlusAudio.setChecked(prefs.getBoolean(hw_plus_audio, true));
        cbSwAudio.setChecked(prefs.getBoolean(sw_audio, false));

        String savedCodec = prefs.getString(custom_codec_path, null);
        if (savedCodec != null) {
            tvCustomCodecPath.setText(savedCodec);
        }

        cbHwPlusVideo.setOnCheckedChangeListener((b, isChecked) -> prefs.edit().putBoolean(hw_plus_video, isChecked).apply());
        cbHwPlusAudio.setOnCheckedChangeListener((b, isChecked) -> prefs.edit().putBoolean(hw_plus_audio, isChecked).apply());
        cbSwAudio.setOnCheckedChangeListener((b, isChecked) -> prefs.edit().putBoolean(sw_audio, isChecked).apply());

        findViewById(R.id.layoutCustomCodec).setOnClickListener(v -> showCustomCodecPickerDialog());
    }

    private void showCustomCodecPickerDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_codec_picker, null);
        TextView tvCurrentPath = dialogView.findViewById(R.id.tvCodecCurrentPath);
        ListView lvFiles = dialogView.findViewById(R.id.lvCodecFiles);

        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (dir == null || !dir.exists()) {
            dir = Environment.getExternalStorageDirectory();
        }
        tvCurrentPath.setText(dir.getAbsolutePath());

        List<String> fileNames = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                fileNames.add(f.getName());
            }
        }
        if (fileNames.isEmpty()) {
            fileNames.add(No custom codec zip/so files found in Download);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileNames);
        lvFiles.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(dialogView)
            .create();

        lvFiles.setOnItemClickListener((parent, view, position, id) -> {
            String selected = fileNames.get(position);
            if (!selected.startsWith(No custom)) {
                prefs.edit().putString(custom_codec_path, selected).apply();
                tvCustomCodecPath.setText(selected);
                Toast.makeText(this, Selected:  + selected, Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btnCancelCodec).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}