package com.greensoft.greenplayer;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MediaFragment extends Fragment {

    public static final int TYPE_VIDEOS = 0;
    public static final int TYPE_FOLDERS = 1;
    public static final int TYPE_MUSIC = 2;

    private static final String ARG_TYPE = "tab_type";
    private int tabType = TYPE_VIDEOS;

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvMediaList;
    private LinearLayout layoutEmpty;
    private TextView tvEmptyTitle;
    private TextView tvEmptySubtitle;
    private Button btnGrantPermission;

    private MediaAdapter adapter;
    private final List<MediaItem> allItems = new ArrayList<>();
    private final List<MediaItem> displayedItems = new ArrayList<>();
    private int currentSortOrder = 0; // 0=date, 1=name, 2=size, 3=duration

    public static MediaFragment newInstance(int type) {
        MediaFragment f = new MediaFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, type);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tabType = getArguments().getInt(ARG_TYPE, TYPE_VIDEOS);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_media_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        rvMediaList = view.findViewById(R.id.rvMediaList);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        tvEmptyTitle = view.findViewById(R.id.tvEmptyTitle);
        tvEmptySubtitle = view.findViewById(R.id.tvEmptySubtitle);
        btnGrantPermission = view.findViewById(R.id.btnGrantPermission);

        rvMediaList.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MediaAdapter(requireContext(), displayedItems, (item, position) -> {
            Intent intent = new Intent(requireContext(), PlayerActivity.class);
            intent.putExtra("path", item.getPath());
            intent.putExtra("title", item.getTitle());
            startActivity(intent);
        });
        rvMediaList.setAdapter(adapter);

        swipeRefresh.setColorSchemeResources(R.color.green_primary, R.color.green_accent);
        swipeRefresh.setOnRefreshListener(this::loadMediaFiles);

        loadMediaFiles();
    }

    public void loadMediaFiles() {
        if (!isAdded()) return;
        allItems.clear();

        Uri collection = (tabType == TYPE_MUSIC)
                ? (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                    ? MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                    : MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                : (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                    ? MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                    : MediaStore.Video.Media.EXTERNAL_CONTENT_URI);

        String[] projection = (tabType == TYPE_MUSIC)
                ? new String[]{
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.DISPLAY_NAME,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.SIZE,
                    MediaStore.Audio.Media.DATE_ADDED,
                    MediaStore.Audio.Media.MIME_TYPE
                }
                : new String[]{
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.DATA,
                    MediaStore.Video.Media.DURATION,
                    MediaStore.Video.Media.SIZE,
                    MediaStore.Video.Media.DATE_ADDED,
                    MediaStore.Video.Media.WIDTH,
                    MediaStore.Video.Media.HEIGHT,
                    MediaStore.Video.Media.MIME_TYPE
                };

        try (Cursor cursor = requireActivity().getContentResolver().query(
                collection,
                projection,
                null,
                null,
                MediaStore.MediaColumns.DATE_ADDED + " DESC")) {

            if (cursor != null) {
                int idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID);
                int nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
                int dataCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                int durationCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DURATION);
                int sizeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE);
                int dateCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED);
                int mimeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE);
                int widthCol = (tabType != TYPE_MUSIC) ? cursor.getColumnIndex(MediaStore.Video.Media.WIDTH) : -1;
                int heightCol = (tabType != TYPE_MUSIC) ? cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT) : -1;

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idCol);
                    String name = cursor.getString(nameCol);
                    String path = cursor.getString(dataCol);
                    long duration = cursor.getLong(durationCol);
                    long size = cursor.getLong(sizeCol);
                    long date = cursor.getLong(dateCol);
                    String mime = cursor.getString(mimeCol);
                    int width = widthCol != -1 ? cursor.getInt(widthCol) : 0;
                    int height = heightCol != -1 ? cursor.getInt(heightCol) : 0;

                    if (path != null) {
                        allItems.add(new MediaItem(id, name, path, duration, size, date, width, height, mime));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        applySort(currentSortOrder);
        swipeRefresh.setRefreshing(false);
    }

    public void filter(String query) {
        displayedItems.clear();
        if (query == null || query.trim().isEmpty()) {
            displayedItems.addAll(allItems);
        } else {
            String lower = query.toLowerCase().trim();
            for (MediaItem item : allItems) {
                if (item.getTitle().toLowerCase().contains(lower)) {
                    displayedItems.add(item);
                }
            }
        }
        updateUiState();
    }

    public void applySort(int sortType) {
        this.currentSortOrder = sortType;
        switch (sortType) {
            case 0: // Date added DESC
                Collections.sort(allItems, (a, b) -> Long.compare(b.getDateAdded(), a.getDateAdded()));
                break;
            case 1: // Name ASC
                Collections.sort(allItems, (a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()));
                break;
            case 2: // Size DESC
                Collections.sort(allItems, (a, b) -> Long.compare(b.getSize(), a.getSize()));
                break;
            case 3: // Duration DESC
                Collections.sort(allItems, (a, b) -> Long.compare(b.getDuration(), a.getDuration()));
                break;
        }
        displayedItems.clear();
        displayedItems.addAll(allItems);
        updateUiState();
    }

    private void updateUiState() {
        if (displayedItems.isEmpty()) {
            rvMediaList.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            rvMediaList.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
            adapter.updateList(displayedItems);
        }
    }
}
