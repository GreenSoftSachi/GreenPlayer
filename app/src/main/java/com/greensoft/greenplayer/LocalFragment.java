package com.greensoft.greenplayer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalFragment extends Fragment {

    public static final int VIEW_ALL_FOLDERS = 0;
    public static final int VIEW_FILES = 1;
    public static final int VIEW_FOLDERS = 2;

    public static final int LAYOUT_LIST = 0;
    public static final int LAYOUT_GRID = 1;

    private int currentViewMode = VIEW_FOLDERS;
    private int currentLayoutMode = LAYOUT_LIST;
    private int currentSortCriteria = 0;

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvMedia;
    private LinearLayout layoutEmpty;
    private LinearLayout layoutFolderBreadcrumb;
    private TextView tvCurrentFolderTitle;
    private ImageView btnBackToFolders;
    private FloatingActionButton fabPlayLast;

    private final List<MediaItem> allVideos = new ArrayList<>();
    private final List<FolderItem> folderList = new ArrayList<>();
    private final List<MediaItem> currentFolderVideos = new ArrayList<>();
    private FolderItem activeFolder = null;

    private MediaAdapter mediaAdapter;
    private FolderAdapter folderAdapter;
    private SharedPreferences prefs;

    public static LocalFragment newInstance() {
        return new LocalFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_local, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = requireContext().getSharedPreferences("green_player_settings", Context.MODE_PRIVATE);

        swipeRefresh = view.findViewById(R.id.swipeRefreshLocal);
        rvMedia = view.findViewById(R.id.rvLocalMedia);
        layoutEmpty = view.findViewById(R.id.layoutEmptyState);
        layoutFolderBreadcrumb = view.findViewById(R.id.layoutFolderBreadcrumb);
        tvCurrentFolderTitle = view.findViewById(R.id.tvCurrentFolderTitle);
        btnBackToFolders = view.findViewById(R.id.btnBackToFolders);
        fabPlayLast = view.findViewById(R.id.fabPlayLast);

        setupQuickTools(view);
        setupRecyclerView();

        btnBackToFolders.setOnClickListener(v -> exitFolderView());
        fabPlayLast.setOnClickListener(v -> playLastPlayedVideo());

        swipeRefresh.setColorSchemeResources(R.color.green_primary, R.color.green_accent);
        swipeRefresh.setOnRefreshListener(this::loadVideos);

        loadVideos();
    }

    private void setupQuickTools(View view) {
        View toolMusic = view.findViewById(R.id.toolMusic);
        View toolTransfer = view.findViewById(R.id.toolTransfer);
        View toolStatus = view.findViewById(R.id.toolStatusSaver);
        View toolPlaylists = view.findViewById(R.id.toolPlaylists);
        View toolCleaner = view.findViewById(R.id.toolCleaner);
        View toolPrivacy = view.findViewById(R.id.toolPrivacy);

        if (toolMusic != null) {
            toolMusic.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).selectTab(1);
                }
            });
        }
        if (toolTransfer != null) {
            toolTransfer.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).selectTab(2);
                }
            });
        }
        if (toolStatus != null) {
            toolStatus.setOnClickListener(v -> Toast.makeText(requireContext(), "Status Saver: Scanning WhatsApp / Telegram statuses", Toast.LENGTH_SHORT).show());
        }
        if (toolPlaylists != null) {
            toolPlaylists.setOnClickListener(v -> Toast.makeText(requireContext(), "My Playlists: 0 custom playlists", Toast.LENGTH_SHORT).show());
        }
        if (toolCleaner != null) {
            toolCleaner.setOnClickListener(v -> Toast.makeText(requireContext(), "Cleaner: Scanning cache and temp video files", Toast.LENGTH_SHORT).show());
        }
        if (toolPrivacy != null) {
            toolPrivacy.setOnClickListener(v -> Toast.makeText(requireContext(), "Privacy: Enter PIN to access Private Folder", Toast.LENGTH_SHORT).show());
        }
    }

    private void setupRecyclerView() {
        if (currentLayoutMode == LAYOUT_GRID && (activeFolder != null || currentViewMode == VIEW_FILES)) {
            rvMedia.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        } else {
            rvMedia.setLayoutManager(new LinearLayoutManager(requireContext()));
        }

        mediaAdapter = new MediaAdapter(requireContext(), (activeFolder != null) ? currentFolderVideos : allVideos, (item, position) -> {
            Intent intent = new Intent(requireContext(), PlayerActivity.class);
            intent.putExtra("path", item.getPath());
            intent.putExtra("title", item.getTitle());
            startActivity(intent);
        });

        folderAdapter = new FolderAdapter(requireContext(), folderList, folder -> enterFolder(folder));
    }

    public void setViewMode(int mode) {
        this.currentViewMode = mode;
        this.activeFolder = null;
        if (layoutFolderBreadcrumb != null) layoutFolderBreadcrumb.setVisibility(View.GONE);
        renderCurrentView();
    }

    public void setLayoutMode(int layoutMode) {
        this.currentLayoutMode = layoutMode;
        setupRecyclerView();
        renderCurrentView();
    }

    public void setSortCriteria(int criteria) {
        this.currentSortCriteria = criteria;
        sortVideos();
        renderCurrentView();
    }

    private void enterFolder(FolderItem folder) {
        activeFolder = folder;
        currentFolderVideos.clear();
        currentFolderVideos.addAll(folder.getItems());
        layoutFolderBreadcrumb.setVisibility(View.VISIBLE);
        tvCurrentFolderTitle.setText(folder.getName() + " (" + folder.getCount() + ")");
        setupRecyclerView();
        rvMedia.setAdapter(mediaAdapter);
    }

    private void exitFolderView() {
        activeFolder = null;
        layoutFolderBreadcrumb.setVisibility(View.GONE);
        renderCurrentView();
    }

    private void renderCurrentView() {
        if (activeFolder != null) {
            rvMedia.setAdapter(mediaAdapter);
            layoutEmpty.setVisibility(currentFolderVideos.isEmpty() ? View.VISIBLE : View.GONE);
            return;
        }

        if (currentViewMode == VIEW_FOLDERS) {
            rvMedia.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvMedia.setAdapter(folderAdapter);
            layoutEmpty.setVisibility(folderList.isEmpty() ? View.VISIBLE : View.GONE);
        } else {
            setupRecyclerView();
            rvMedia.setAdapter(mediaAdapter);
            layoutEmpty.setVisibility(allVideos.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    public void loadVideos() {
        if (!isAdded()) return;
        allVideos.clear();
        folderList.clear();

        Uri collection = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                ? MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                : MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        String[] projection = new String[]{
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.WIDTH,
                MediaStore.Video.Media.HEIGHT,
                MediaStore.Video.Media.MIME_TYPE
        };

        Map<String, FolderItem> folderMap = new HashMap<>();

        try (Cursor cursor = requireContext().getContentResolver().query(
                collection, projection, null, null, MediaStore.Video.Media.DATE_ADDED + " DESC")) {

            if (cursor != null) {
                int idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                int titleCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
                int dataCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                int durCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
                int sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
                int dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED);
                int widthCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH);
                int heightCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT);
                int mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE);

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idCol);
                    String title = cursor.getString(titleCol);
                    String path = cursor.getString(dataCol);
                    long duration = cursor.getLong(durCol);
                    long size = cursor.getLong(sizeCol);
                    long dateAdded = cursor.getLong(dateCol);
                    int width = cursor.getInt(widthCol);
                    int height = cursor.getInt(heightCol);
                    String mimeType = cursor.getString(mimeCol);

                    MediaItem item = new MediaItem(id, title, path, duration, size, dateAdded, width, height, mimeType);
                    allVideos.add(item);

                    if (path != null) {
                        File file = new File(path);
                        File parent = file.getParentFile();
                        if (parent != null) {
                            String parentPath = parent.getAbsolutePath();
                            FolderItem fItem = folderMap.get(parentPath);
                            if (fItem == null) {
                                fItem = new FolderItem(parentPath);
                                folderMap.put(parentPath, fItem);
                            }
                            fItem.addItem(item);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        folderList.addAll(folderMap.values());
        Collections.sort(folderList, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));

        sortVideos();
        renderCurrentView();

        if (swipeRefresh != null) {
            swipeRefresh.setRefreshing(false);
        }
    }

    private void sortVideos() {
        switch (currentSortCriteria) {
            case 0: // Title
                Collections.sort(allVideos, (a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()));
                break;
            case 1: // Date
                Collections.sort(allVideos, (a, b) -> Long.compare(b.getDateAdded(), a.getDateAdded()));
                break;
            case 2: // Size
                Collections.sort(allVideos, (a, b) -> Long.compare(b.getSize(), a.getSize()));
                break;
            case 3: // Length/Duration
                Collections.sort(allVideos, (a, b) -> Long.compare(b.getDuration(), a.getDuration()));
                break;
            default:
                break;
        }
    }

    private void playLastPlayedVideo() {
        SharedPreferences resumePrefs = requireContext().getSharedPreferences("player_resume_prefs", Context.MODE_PRIVATE);
        String lastPath = resumePrefs.getString("last_played_path", null);
        String lastTitle = resumePrefs.getString("last_played_title", null);

        if (lastPath != null && new File(lastPath).exists()) {
            Intent intent = new Intent(requireContext(), PlayerActivity.class);
            intent.putExtra("path", lastPath);
            intent.putExtra("title", lastTitle);
            startActivity(intent);
        } else if (!allVideos.isEmpty()) {
            MediaItem first = allVideos.get(0);
            Intent intent = new Intent(requireContext(), PlayerActivity.class);
            intent.putExtra("path", first.getPath());
            intent.putExtra("title", first.getTitle());
            startActivity(intent);
        } else {
            Toast.makeText(requireContext(), "No videos available to play", Toast.LENGTH_SHORT).show();
        }
    }

    public void filter(String query) {
        if (query == null || query.trim().isEmpty()) {
            loadVideos();
            return;
        }
        String lower = query.toLowerCase();
        List<MediaItem> filtered = new ArrayList<>();
        for (MediaItem item : allVideos) {
            if (item.getTitle().toLowerCase().contains(lower)) {
                filtered.add(item);
            }
        }
        allVideos.clear();
        allVideos.addAll(filtered);
        currentViewMode = VIEW_FILES;
        renderCurrentView();
    }
}
