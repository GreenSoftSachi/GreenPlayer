package com.greensoft.greenplayer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class TransferFragment extends Fragment {

    public static TransferFragment newInstance() {
        return new TransferFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transfer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.cardSend).setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                .setTitle("Send Files")
                .setMessage("Select videos or music from your Local library to send via high-speed Wi-Fi Direct.")
                .setPositiveButton("Select Media", (d, w) -> {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).selectTab(0);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
        });

        view.findViewById(R.id.cardReceive).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Waiting for incoming sender on local hotspot...", Toast.LENGTH_LONG).show();
        });

        view.findViewById(R.id.cardWebShare).setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                .setTitle("Web / PC Share")
                .setMessage("Connect your PC and Phone to the same Wi-Fi, then visit:\n\nhttp://192.168.1.100:8080\n\nin any browser to download videos directly.")
                .setPositiveButton("Start Web Server", (d, w) -> {
                    Toast.makeText(requireContext(), "Web Share server listening on port 8080", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Close", null)
                .show();
        });
    }
}
