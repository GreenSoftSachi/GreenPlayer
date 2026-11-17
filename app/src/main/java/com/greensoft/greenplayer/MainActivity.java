package com.greensoft.greenplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQ_CODE = 1001;

    private ViewPager2 mainViewPager;
    private BottomNavigationView mainBottomNav;
    private TextView tvMainTitle;
    private EditText etSearch;
    private ImageButton btnToggleViewMode, btnSearch, btnViewSortDialog;

    private LocalFragment localFragment;
    private MusicFragment musicFragment;
    private TransferFragment transferFragment;
    private MeFragment meFragment;

    private final String[] tabTitles = new String[]{"Folders", "Music", "Transfer", "Me"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupViewPager();
        setupBottomNav();
        setupToolbarActions();

        checkAndRequestPermissions();
    }

    private void initViews() {
        mainViewPager = findViewById(R.id.mainViewPager);
        mainBottomNav = findViewById(R.id.mainBottomNav);
        tvMainTitle = findViewById(R.id.tvMainTitle);
        etSearch = findViewById(R.id.etSearch);
        btnToggleViewMode = findViewById(R.id.btnToggleViewMode);
        btnSearch = findViewById(R.id.btnSearch);
        btnViewSortDialog = findViewById(R.id.btnViewSortDialog);
    }

    private void setupViewPager() {
        localFragment = LocalFragment.newInstance();
        musicFragment = MusicFragment.newInstance();
        transferFragment = TransferFragment.newInstance();
        meFragment = MeFragment.newInstance();

        List<Fragment> fragments = new ArrayList<>();
        fragments.add(localFragment);
        fragments.add(musicFragment);
        fragments.add(transferFragment);
        fragments.add(meFragment);

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

        mainViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tvMainTitle.setText(tabTitles[position]);
                int menuId = R.id.nav_local;
                if (position == 1) menuId = R.id.nav_music;
                else if (position == 2) menuId = R.id.nav_transfer;
                else if (position == 3) menuId = R.id.nav_me;

                mainBottomNav.setSelectedItemId(menuId);

                // Show top toolbar controls only on Local/Videos and Music tabs
                boolean showTools = (position == 0 || position == 1);
                btnToggleViewMode.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
                btnViewSortDialog.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
                btnSearch.setVisibility(showTools ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void setupBottomNav() {
        mainBottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_local) {
                mainViewPager.setCurrentItem(0, true);
                return true;
            } else if (itemId == R.id.nav_music) {
                mainViewPager.setCurrentItem(1, true);
                return true;
            } else if (itemId == R.id.nav_transfer) {
                mainViewPager.setCurrentItem(2, true);
                return true;
            } else if (itemId == R.id.nav_me) {
                mainViewPager.setCurrentItem(3, true);
                return true;
            }
            return false;
        });
    }

    public void selectTab(int position) {
        if (mainViewPager != null) {
            mainViewPager.setCurrentItem(position, true);
        }
    }

    private void setupToolbarActions() {
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
                if (mainViewPager.getCurrentItem() == 0 && localFragment != null) {
                    localFragment.filter(s.toString());
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnToggleViewMode.setOnClickListener(v -> {
            if (localFragment != null) {
                // Cycle Folders -> Files
                if (tvMainTitle.getText().toString().equals("Folders")) {
                    tvMainTitle.setText("Videos");
                    localFragment.setViewMode(LocalFragment.VIEW_FILES);
                } else {
                    tvMainTitle.setText("Folders");
                    localFragment.setViewMode(LocalFragment.VIEW_FOLDERS);
                }
            }
        });

        btnViewSortDialog.setOnClickListener(v -> showViewSortDialog());
    }

    private void showViewSortDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_view_sort, null);

        TextView btnViewAll = dialogView.findViewById(R.id.btnViewAllFolders);
        TextView btnViewFiles = dialogView.findViewById(R.id.btnViewFiles);
        TextView btnViewFolders = dialogView.findViewById(R.id.btnViewFolders);

        TextView btnList = dialogView.findViewById(R.id.btnLayoutList);
        TextView btnGrid = dialogView.findViewById(R.id.btnLayoutGrid);

        View sortTitle = dialogView.findViewById(R.id.sortTitle);
        View sortDate = dialogView.findViewById(R.id.sortDate);
        View sortPlayedTime = dialogView.findViewById(R.id.sortPlayedTime);
        View sortLength = dialogView.findViewById(R.id.sortLength);
        View sortSize = dialogView.findViewById(R.id.sortSize);
        View sortResolution = dialogView.findViewById(R.id.sortResolution);

        final int[] chosenView = new int[]{LocalFragment.VIEW_FOLDERS};
        final int[] chosenLayout = new int[]{LocalFragment.LAYOUT_LIST};
        final int[] chosenSort = new int[]{0};

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (btnViewAll != null) btnViewAll.setOnClickListener(v -> chosenView[0] = LocalFragment.VIEW_ALL_FOLDERS);
        if (btnViewFiles != null) btnViewFiles.setOnClickListener(v -> chosenView[0] = LocalFragment.VIEW_FILES);
        if (btnViewFolders != null) btnViewFolders.setOnClickListener(v -> chosenView[0] = LocalFragment.VIEW_FOLDERS);

        if (btnList != null) btnList.setOnClickListener(v -> chosenLayout[0] = LocalFragment.LAYOUT_LIST);
        if (btnGrid != null) btnGrid.setOnClickListener(v -> chosenLayout[0] = LocalFragment.LAYOUT_GRID);

        if (sortTitle != null) sortTitle.setOnClickListener(v -> chosenSort[0] = 0);
        if (sortDate != null) sortDate.setOnClickListener(v -> chosenSort[0] = 1);
        if (sortSize != null) sortSize.setOnClickListener(v -> chosenSort[0] = 2);
        if (sortLength != null) sortLength.setOnClickListener(v -> chosenSort[0] = 3);
        if (sortPlayedTime != null) sortPlayedTime.setOnClickListener(v -> chosenSort[0] = 1);
        if (sortResolution != null) sortResolution.setOnClickListener(v -> chosenSort[0] = 2);

        View btnCancel = dialogView.findViewById(R.id.btnCancelSort);
        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        View btnDone = dialogView.findViewById(R.id.btnDoneSort);
        if (btnDone != null) {
            btnDone.setOnClickListener(v -> {
                if (localFragment != null) {
                    localFragment.setViewMode(chosenView[0]);
                    localFragment.setLayoutMode(chosenLayout[0]);
                    localFragment.setSortCriteria(chosenSort[0]);
                    tvMainTitle.setText(chosenView[0] == LocalFragment.VIEW_FOLDERS ? "Folders" : "Videos");
                }
                dialog.dismiss();
            });
        }

        dialog.show();
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
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                if (localFragment != null) localFragment.loadVideos();
                if (musicFragment != null) musicFragment.loadAudioFiles();
            } else {
                Toast.makeText(this, "Storage permission is required to list media files", Toast.LENGTH_LONG).show();
            }
        }
    }
}
