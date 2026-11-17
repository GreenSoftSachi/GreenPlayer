package com.greensoft.greenplayer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.ViewHolder> {

    private final Context context;
    private List<MediaItem> items;
    private final SharedPreferences prefs;

    public interface OnItemClickListener {
        void onItemClick(MediaItem item, int position);
    }

    private final OnItemClickListener clickListener;

    public MediaAdapter(Context context, List<MediaItem> items, OnItemClickListener clickListener) {
        this.context = context;
        this.items = items != null ? items : new ArrayList<>();
        this.clickListener = clickListener;
        this.prefs = context.getSharedPreferences("green_player_resume", Context.MODE_PRIVATE);
    }

    public void updateList(List<MediaItem> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MediaItem item = items.get(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvDurationBadge.setText(item.getFormattedDuration());
        holder.tvSize.setText(item.getFormattedSize());
        holder.tvDate.setText(item.getFormattedDate());

        // Resolution badge
        String resLabel = item.getResolutionLabel();
        if (!resLabel.isEmpty()) {
            holder.tvResolutionBadge.setVisibility(View.VISIBLE);
            holder.tvResolutionBadge.setText(resLabel);
        } else {
            holder.tvResolutionBadge.setVisibility(View.GONE);
        }

        // Check resume progress
        long lastPosition = prefs.getLong(item.getPath(), 0);
        if (lastPosition > 0 && item.getDuration() > 0) {
            int progress = (int) ((lastPosition * 100) / item.getDuration());
            holder.pbResumeProgress.setVisibility(View.VISIBLE);
            holder.pbResumeProgress.setProgress(progress);
        } else {
            holder.pbResumeProgress.setVisibility(View.GONE);
        }

        // Load thumbnail with Glide
        Glide.with(context)
                .load(item.getPath())
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .centerCrop()
                .placeholder(R.color.bg_surface)
                .error(android.R.drawable.ic_menu_slideshow)
                .into(holder.ivThumbnail);

        // Click to play
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(item, holder.getAdapterPosition());
            }
        });

        // 3-dots popup menu
        holder.btnItemMore.setOnClickListener(v -> showPopupMenu(v, item, holder.getAdapterPosition()));
    }

    private void showPopupMenu(View anchor, MediaItem item, int position) {
        PopupMenu popup = new PopupMenu(context, anchor);
        popup.getMenu().add(0, 1, 0, context.getString(R.string.play));
        popup.getMenu().add(0, 2, 1, context.getString(R.string.share));
        popup.getMenu().add(0, 3, 2, context.getString(R.string.video_properties));
        popup.getMenu().add(0, 4, 3, context.getString(R.string.delete));

        popup.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case 1:
                    if (clickListener != null) clickListener.onItemClick(item, position);
                    return true;
                case 2:
                    shareVideo(item);
                    return true;
                case 3:
                    showPropertiesDialog(item);
                    return true;
                case 4:
                    confirmDelete(item, position);
                    return true;
            }
            return false;
        });
        popup.show();
    }

    private void shareVideo(MediaItem item) {
        try {
            File file = new File(item.getPath());
            Uri uri = FileProvider.getUriForFile(context, "com.greensoft.greenplayer.fileprovider", file);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType(item.getMimeType() != null ? item.getMimeType() : "video/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share)));
        } catch (Exception e) {
            Toast.makeText(context, "Could not share file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showPropertiesDialog(MediaItem item) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_properties, null);
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .create();

        TextView tvName = dialogView.findViewById(R.id.tvPropName);
        TextView tvPath = dialogView.findViewById(R.id.tvPropPath);
        TextView tvSize = dialogView.findViewById(R.id.tvPropSize);
        TextView tvDuration = dialogView.findViewById(R.id.tvPropDuration);
        TextView tvRes = dialogView.findViewById(R.id.tvPropResolution);

        tvName.setText("File: " + item.getTitle());
        tvPath.setText("Path: " + item.getPath());
        tvSize.setText("Size: " + item.getFormattedSize());
        tvDuration.setText("Duration: " + item.getFormattedDuration());
        tvRes.setText("Resolution: " + (item.getWidth() > 0 ? (item.getWidth() + " x " + item.getHeight()) : "Unknown"));

        dialogView.findViewById(R.id.btnCloseProps).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void confirmDelete(MediaItem item, int position) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.delete))
                .setMessage("Are you sure you want to delete \"" + item.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    try {
                        File file = new File(item.getPath());
                        if (file.exists() && file.delete()) {
                            items.remove(position);
                            notifyItemRemoved(position);
                            Toast.makeText(context, "File deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Cannot delete file (Protected storage)", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvDurationBadge;
        ProgressBar pbResumeProgress;
        TextView tvTitle;
        TextView tvResolutionBadge;
        TextView tvSize;
        TextView tvDate;
        ImageButton btnItemMore;

        ViewHolder(View v) {
            super(v);
            ivThumbnail = v.findViewById(R.id.ivThumbnail);
            tvDurationBadge = v.findViewById(R.id.tvDurationBadge);
            pbResumeProgress = v.findViewById(R.id.pbResumeProgress);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvResolutionBadge = v.findViewById(R.id.tvResolutionBadge);
            tvSize = v.findViewById(R.id.tvSize);
            tvDate = v.findViewById(R.id.tvDate);
            btnItemMore = v.findViewById(R.id.btnItemMore);
        }
    }
}
