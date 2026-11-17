package com.greensoft.greenplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQ_CODE = 1001;

    private ViewPager2 mainViewPager;
    private TabLayout mainTabLayout;
    private EditText etSearch;
    private ImageButton btnSearch, btnSort, btnSettings;

    private final List<MediaFragment> fragments = new ArrayList<>();
    private final String[] tabTitles = new String[]{"🎬 Videos", "📁 Folders", "🎵 Audio"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupTabs();
        setupSearch();
        setupButtons();

        checkAndRequestPermissions();
    }

    private void initViews() {
        mainViewPager = findViewById(R.id.mainViewPager);
        mainTabLayout = findViewById(R.id.mainTabLayout);
        etSearch = findViewById(R.id.etSearch);
        btnSearch = findViewById(R.id.btnSearch);
        btnSort = findViewById(R.id.btnSort);
        btnSettings = findViewById(R.id.btnSettings);
    }

    private void setupTabs() {
        fragments.clear();
        fragments.add(MediaFragment.newInstance(MediaFragment.TYPE_VIDEOS));
        fragments.add(MediaFragment.newInstance(MediaFragment.TYPE_FOLDERS));
        fragments.add(MediaFragment.newInstance(MediaFragment.TYPE_MUSIC));

        mainViewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return fragments.get(position);
            }

            @Override
            public int getItemCount() {
                return fragments.size();
            }
        });

        new TabLayoutMediator(mainTabLayout, mainViewPager, (tab, position) -> {
            tab.setText(tabTitles[position]);
        }).attach();
    }

    private void setupSearch() {
        btnSearch.setOnClickListener(v -> {
            if (etSearch.getVisibility() == View.VISIBLE) {
                etSearch.setVisibility(View.GONE);
                etSearch.setText("");
            } else {
                etSearch.setVisibility(View.VISIBLE);
                etSearch.requestFocus();
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                int currentTab = mainViewPager.getCurrentItem();
                if (currentTab >= 0 && currentTab < fragments.size()) {
                    fragments.get(currentTab).filter(s.toString());
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupButtons() {
        btnSort.setOnClickListener(v -> showSortDialog());
        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
    }

    private void showSortDialog() {
        String[] sortOptions = new String[]{
                getString(R.string.sort_date),
                getString(R.string.sort_name),
                getString(R.string.sort_size),
                getString(R.string.sort_duration)
        };

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.sort_by))
                .setItems(sortOptions, (dialog, which) -> {
                    int currentTab = mainViewPager.getCurrentItem();
                    if (currentTab >= 0 && currentTab < fragments.size()) {
                        fragments.get(currentTab).applySort(which);
                    }
                })
                .show();
    }

    private void checkAndRequestPermissions() {
        List<String> neededPermissions = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(Manifest.permission.READ_MEDIA_VIDEO);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(Manifest.permission.READ_MEDIA_AUDIO);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }

        if (!neededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, neededPermissions.toArray(new String[0]), PERMISSION_REQ_CODE);
        } else {
            refreshAllFragments();
        }
    }

    private void refreshAllFragments() {
        for (MediaFragment f : fragments) {
            if (f != null && f.isAdded()) {
                f.loadMediaFiles();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_CODE) {
            boolean allGranted = true;
            for (int res : grantResults) {
                if (res != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                refreshAllFragments();
            } else {
                Toast.makeText(this, getString(R.string.permission_required), Toast.LENGTH_LONG).show();
            }
        }
    }
}
