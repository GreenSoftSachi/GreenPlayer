package com.greensoft.greenplayer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MediaItem {
    private final long id;
    private final String title;
    private final String path;
    private final long duration;
    private final long size;
    private final long dateAdded;
    private final int width;
    private final int height;
    private final String mimeType;

    public MediaItem(long id, String title, String path, long duration, long size, long dateAdded, int width, int height, String mimeType) {
        this.id = id;
        this.title = title != null ? title : (path != null ? new File(path).getName() : "Unknown");
        this.path = path;
        this.duration = duration;
        this.size = size;
        this.dateAdded = dateAdded;
        this.width = width;
        this.height = height;
        this.mimeType = mimeType;
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getPath() { return path; }
    public long getDuration() { return duration; }
    public long getSize() { return size; }
    public long getDateAdded() { return dateAdded; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public String getMimeType() { return mimeType; }

    public String getFormattedDuration() {
        if (duration <= 0) return "00:00";
        long totalSeconds = duration / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;

        if (hours > 0) {
            return String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.US, "%02d:%02d", minutes, seconds);
        }
    }

    public String getFormattedSize() {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        if (digitGroups >= units.length) digitGroups = units.length - 1;
        return String.format(Locale.US, "%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    public String getFormattedDate() {
        if (dateAdded <= 0) return "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return sdf.format(new Date(dateAdded * 1000L));
        } catch (Exception e) {
            return "";
        }
    }

    public String getResolutionLabel() {
        if (width >= 3840 || height >= 2160) return "4K";
        if (width >= 1920 || height >= 1080) return "FHD";
        if (width >= 1280 || height >= 720) return "HD";
        if (width > 0 && height > 0) return width + "p";
        return "";
    }
}
