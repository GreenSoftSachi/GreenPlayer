package com.greensoft.greenplayer;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MusicFragment extends Fragment {

    private RecyclerView rvMusic;
    private TextView tvSongCount;
    private EditText etSearchMusic;
    private TabLayout tabLayoutMusic;

    private MediaAdapter adapter;
    private final List<MediaItem> allTracks = new ArrayList<>();
    private final List<MediaItem> displayedTracks = new ArrayList<>();

    public static MusicFragment newInstance() {
        return new MusicFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_music, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvMusic = view.findViewById(R.id.rvMusic);
        tvSongCount = view.findViewById(R.id.tvSongCount);
        etSearchMusic = view.findViewById(R.id.etSearchMusic);
        tabLayoutMusic = view.findViewById(R.id.tabLayoutMusic);

        setupSubTabs();

        rvMusic.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MediaAdapter(requireContext(), displayedTracks, (item, position) -> {
            Intent intent = new Intent(requireContext(), PlayerActivity.class);
            intent.putExtra("path", item.getPath());
            intent.putExtra("title", item.getTitle());
            startActivity(intent);
        });
        rvMusic.setAdapter(adapter);

        view.findViewById(R.id.btnShuffleAll).setOnClickListener(v -> shuffleAllSongs());

        if (etSearchMusic != null) {
            etSearchMusic.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterSongs(s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        loadAudioFiles();
    }

    private void setupSubTabs() {
        tabLayoutMusic.addTab(tabLayoutMusic.newTab().setText("Tracks"));
        tabLayoutMusic.addTab(tabLayoutMusic.newTab().setText("Playlists"));
        tabLayoutMusic.addTab(tabLayoutMusic.newTab().setText("Albums"));
        tabLayoutMusic.addTab(tabLayoutMusic.newTab().setText("Artists"));
        tabLayoutMusic.addTab(tabLayoutMusic.newTab().setText("Folders"));

        tabLayoutMusic.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Toast.makeText(requireContext(), tab.getText() + " view", Toast.LENGTH_SHORT).show();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    public void loadAudioFiles() {
        if (!isAdded()) return;
        allTracks.clear();
        displayedTracks.clear();

        Uri collection = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                ? MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                : MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.MIME_TYPE
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        try (Cursor cursor = requireContext().getContentResolver().query(
                collection, projection, selection, null, MediaStore.Audio.Media.TITLE + " ASC")) {

            if (cursor != null) {
                int idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                int durCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
                int sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
                int dateCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED);
                int mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE);

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idCol);
                    String title = cursor.getString(titleCol);
                    String path = cursor.getString(dataCol);
                    long duration = cursor.getLong(durCol);
                    long size = cursor.getLong(sizeCol);
                    long dateAdded = cursor.getLong(dateCol);
                    String mimeType = cursor.getString(mimeCol);

                    MediaItem item = new MediaItem(id, title, path, duration, size, dateAdded, 0, 0, mimeType);
                    allTracks.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        displayedTracks.addAll(allTracks);
        tvSongCount.setText("Shuffle All (" + displayedTracks.size() + " songs)");
        adapter.notifyDataSetChanged();
    }

    private void shuffleAllSongs() {
        if (displayedTracks.isEmpty()) {
            Toast.makeText(requireContext(), "No audio songs found", Toast.LENGTH_SHORT).show();
            return;
        }
        List<MediaItem> shuffled = new ArrayList<>(displayedTracks);
        Collections.shuffle(shuffled);
        MediaItem first = shuffled.get(0);
        Intent intent = new Intent(requireContext(), PlayerActivity.class);
        intent.putExtra("path", first.getPath());
        intent.putExtra("title", first.getTitle());
        startActivity(intent);
    }

    private void filterSongs(String query) {
        displayedTracks.clear();
        if (query == null || query.trim().isEmpty()) {
            displayedTracks.addAll(allTracks);
        } else {
            String lower = query.toLowerCase();
            for (MediaItem item : allTracks) {
                if (item.getTitle().toLowerCase().contains(lower)) {
                    displayedTracks.add(item);
                }
            }
        }
        tvSongCount.setText("Shuffle All (" + displayedTracks.size() + " songs)");
        adapter.notifyDataSetChanged();
    }
}
