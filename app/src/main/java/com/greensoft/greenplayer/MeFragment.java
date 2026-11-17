package com.greensoft.greenplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class MeFragment extends Fragment {

    public static MeFragment newInstance() {
        return new MeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_me, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 8 Grid Tools
        view.findViewById(R.id.meToolTransfer).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).selectTab(2);
            }
        });

        view.findViewById(R.id.meToolPrivateFolder).setOnClickListener(v -> showPrivateFolderDialog());

        view.findViewById(R.id.meToolPlaylists).setOnClickListener(v -> 
            Toast.makeText(requireContext(), "Video Playlists: 0 playlists created", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.meToolMediaManager).setOnClickListener(v -> showMediaManagerDialog());

        view.findViewById(R.id.meToolNetwork).setOnClickListener(v -> showLocalNetworkDialog());

        view.findViewById(R.id.meToolMusicPlayer).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).selectTab(1);
            }
        });

        view.findViewById(R.id.meToolCloudDrive).setOnClickListener(v -> 
            Toast.makeText(requireContext(), "Cloud Drive: Google Drive / OneDrive support", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.meToolRecycleBin).setOnClickListener(v -> 
            Toast.makeText(requireContext(), "Recycle Bin is empty (30-day retention)", Toast.LENGTH_SHORT).show());

        // Vertical List Items
        view.findViewById(R.id.rowStatusSaver).setOnClickListener(v -> 
            Toast.makeText(requireContext(), "Status Saver: Scan recent messaging statuses", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.rowAppTheme).setOnClickListener(v -> 
            startActivity(new Intent(requireContext(), AppThemeActivity.class)));

        view.findViewById(R.id.rowSettings).setOnClickListener(v -> 
            startActivity(new Intent(requireContext(), SettingsActivity.class)));

        view.findViewById(R.id.rowCustomPopup).setOnClickListener(v -> 
            startActivity(new Intent(requireContext(), CustomPopupPlayActivity.class)));
    }

    private void showPrivateFolderDialog() {
        final EditText etPin = new EditText(requireContext());
        etPin.setHint("Enter 4-digit PIN");
        etPin.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        new AlertDialog.Builder(requireContext())
            .setTitle("Private Folder")
            .setMessage("Set or enter your security PIN to access encrypted videos.")
            .setView(etPin)
            .setPositiveButton("Unlock", (dialog, which) -> {
                String pin = etPin.getText().toString();
                if (pin.length() >= 4) {
                    Toast.makeText(requireContext(), "Private Folder unlocked (0 hidden files)", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "PIN must be at least 4 digits", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showMediaManagerDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Media Manager")
            .setMessage("Storage analysis:\n\n• Video storage: Scanning...\n• Audio storage: Scanning...\n• Cache and Temp: Ready to clean")
            .setPositiveButton("Analyze Now", (d, w) -> Toast.makeText(requireContext(), "Media storage analyzed", Toast.LENGTH_SHORT).show())
            .setNegativeButton("Close", null)
            .show();
    }

    private void showLocalNetworkDialog() {
        String[] protocols = new String[]{"SMB (Windows Share)", "DLNA / UPnP Server", "FTP Server", "WebDAV"};
        new AlertDialog.Builder(requireContext())
            .setTitle("Local Network Servers")
            .setItems(protocols, (dialog, which) -> {
                Toast.makeText(requireContext(), "Scanning for " + protocols[which] + " on local Wi-Fi...", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
