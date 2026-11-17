package com.greensoft.greenplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FolderItem {
    private final String path;
    private final String name;
    private final List<MediaItem> items = new ArrayList<>();

    public FolderItem(String path) {
        this.path = path;
        this.name = (path != null) ? new File(path).getName() : Unknown;
    }

    public String getPath() { return path; }
    public String getName() { return name; }
    public List<MediaItem> getItems() { return items; }
    public int getCount() { return items.size(); }

    public void addItem(MediaItem item) {
        items.add(item);
    }
}