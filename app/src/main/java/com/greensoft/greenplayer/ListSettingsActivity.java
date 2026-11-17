package com.greensoft.greenplayer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ListSettingsActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_list);

        prefs = getSharedPreferences(green_player_settings, MODE_PRIVATE);

        ImageView btnBack = findViewById(R.id.btnBackListSettings);
        btnBack.setOnClickListener(v -> finish());

        CheckBox cbLastMedia = findViewById(R.id.cbLastMedia);
        CheckBox cbScrollLastMedia = findViewById(R.id.cbScrollLastMedia);
        CheckBox cbSelectThumbnail = findViewById(R.id.cbSelectThumbnail);
        CheckBox cbFloatingButton = findViewById(R.id.cbFloatingButton);
        CheckBox cbShowHistory = findViewById(R.id.cbShowHistory);

        cbLastMedia.setChecked(prefs.getBoolean(list_last_media, true));
        cbScrollLastMedia.setChecked(prefs.getBoolean(list_scroll_last, false));
        cbSelectThumbnail.setChecked(prefs.getBoolean(list_select_thumb, false));
        cbFloatingButton.setChecked(prefs.getBoolean(list_floating_btn, true));
        cbShowHistory.setChecked(prefs.getBoolean(list_show_history, true));

        cbLastMedia.setOnCheckedChangeListener((b, isChecked) -> prefs.edit().putBoolean(list_last_media, isChecked).apply());
        cbScrollLastMedia.setOnCheckedChangeListener((b, isChecked) -> prefs.edit().putBoolean(list_scroll_last, isChecked).apply());
        cbSelectThumbnail.setOnCheckedChangeListener((b, isChecked) -> prefs.edit().putBoolean(list_select_thumb, isChecked).apply());
        cbFloatingButton.setOnCheckedChangeListener((b, isChecked) -> prefs.edit().putBoolean(list_floating_btn, isChecked).apply());
        cbShowHistory.setOnCheckedChangeListener((b, isChecked) -> prefs.edit().putBoolean(list_show_history, isChecked).apply());

        findViewById(R.id.layoutPeriodNew).setOnClickListener(v -> {
            String[] periods = new String[]{None, 1 day, 3 days, 7 days (Default), 15 days, 30 days};
            new AlertDialog.Builder(this)
                .setTitle(Period tagged as "NEW")
                .setItems(periods, (dialog, which) -> {
                    prefs.edit().putString(period_new, periods[which]).apply();
                    Toast.makeText(this, Set:  + periods[which], Toast.LENGTH_SHORT).show();
                })
                .show();
        });

        findViewById(R.id.layoutScanFolders).setOnClickListener(v -> {
            Toast.makeText(this, All device video & audio folders are scanned, Toast.LENGTH_SHORT).show();
        });
    }
}