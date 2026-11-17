package com.greensoft.greenplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {

    public interface OnFolderClickListener {
        void onFolderClick(FolderItem folder);
    }

    private final Context context;
    private final List<FolderItem> folders;
    private final OnFolderClickListener listener;

    public FolderAdapter(Context context, List<FolderItem> folders, OnFolderClickListener listener) {
        this.context = context;
        this.folders = folders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_folder, parent, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        FolderItem folder = folders.get(position);
        holder.tvFolderName.setText(folder.getName());

        int count = folder.getCount();
        String badgeText = count > 99 ? "99+" : String.valueOf(count);
        holder.tvFolderBadge.setText(badgeText);

        String countSubtext = count == 1 ? "1 video" : count + " videos";
        holder.tvFolderVideoCount.setText(countSubtext);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFolderClick(folder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    static class FolderViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFolderIcon;
        TextView tvFolderBadge;
        TextView tvFolderName;
        TextView tvFolderVideoCount;

        FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFolderIcon = itemView.findViewById(R.id.ivFolderIcon);
            tvFolderBadge = itemView.findViewById(R.id.tvFolderBadge);
            tvFolderName = itemView.findViewById(R.id.tvFolderName);
            tvFolderVideoCount = itemView.findViewById(R.id.tvFolderVideoCount);
        }
    }
}
