package com.greensoft.greenplayer;

import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Rational;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.common.TrackGroup;
import androidx.media3.common.TrackSelectionOverride;
import androidx.media3.common.Tracks;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlayerActivity extends AppCompatActivity {

    private PlayerView playerView;
    private ExoPlayer player;
    private View gestureTouchView;
    private FrameLayout controlsContainer;
    private LinearLayout topBar, bottomBar;
    private LinearLayout hudVolume, hudBrightness, hudSeek;
    private ProgressBar pbVolume, pbBrightness;
    private TextView tvVolumePercent, tvBrightnessPercent, tvSeekDiff, tvSeekTargetTime;
    private TextView tvVideoTitle, tvCurrentPosition, tvDuration;
    private SeekBar playerSeekBar;
    private ImageButton btnPlayPause, btnRewind, btnForward, btnBack;
    private ImageButton btnAudioTrack, btnSubtitle, btnAspectRatio, btnSpeed, btnPip, btnLock, btnUnlockFloating;

    private AudioManager audioManager;
    private int maxVolume;
    private float currentBrightness = -1f;

    private String videoPath;
    private String videoTitle;
    private SharedPreferences resumePrefs;

    private boolean isControlsShowing = true;
    private boolean isLocked = false;
    private final Handler controlsHandler = new Handler(Looper.getMainLooper());
    private final Runnable autoHideRunnable = this::hideControls;

    // Gesture tracking
    private float touchStartX, touchStartY;
    private boolean isSwipeVolume = false;
    private boolean isSwipeBrightness = false;
    private boolean isSwipeSeek = false;
    private long swipeSeekTarget = 0;
    private static final int GESTURE_THRESHOLD = 30;

    // Aspect ratio modes: 0=FIT, 1=CROP, 2=FILL/STRETCH
    private int currentAspectMode = 0;
    private float currentSpeed = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_player);

        videoPath = getIntent().getStringExtra("path");
        videoTitle = getIntent().getStringExtra("title");
        if (videoTitle == null && videoPath != null) {
            videoTitle = new File(videoPath).getName();
        }

        resumePrefs = getSharedPreferences("green_player_resume", Context.MODE_PRIVATE);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        }

        initViews();
        setupPlayer();
        setupGestures();
        setupClickListeners();
    }

    private void initViews() {
        playerView = findViewById(R.id.playerView);
        gestureTouchView = findViewById(R.id.gestureTouchView);
        controlsContainer = findViewById(R.id.controlsContainer);
        topBar = findViewById(R.id.topBar);
        bottomBar = findViewById(R.id.bottomBar);

        hudVolume = findViewById(R.id.hudVolume);
        hudBrightness = findViewById(R.id.hudBrightness);
        hudSeek = findViewById(R.id.hudSeek);
        pbVolume = findViewById(R.id.pbVolume);
        pbBrightness = findViewById(R.id.pbBrightness);
        tvVolumePercent = findViewById(R.id.tvVolumePercent);
        tvBrightnessPercent = findViewById(R.id.tvBrightnessPercent);
        tvSeekDiff = findViewById(R.id.tvSeekDiff);
        tvSeekTargetTime = findViewById(R.id.tvSeekTargetTime);

        tvVideoTitle = findViewById(R.id.tvVideoTitle);
        tvCurrentPosition = findViewById(R.id.tvCurrentPosition);
        tvDuration = findViewById(R.id.tvDuration);
        playerSeekBar = findViewById(R.id.playerSeekBar);

        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnRewind = findViewById(R.id.btnRewind);
        btnForward = findViewById(R.id.btnForward);
        btnBack = findViewById(R.id.btnBack);
        btnAudioTrack = findViewById(R.id.btnAudioTrack);
        btnSubtitle = findViewById(R.id.btnSubtitle);
        btnAspectRatio = findViewById(R.id.btnAspectRatio);
        btnSpeed = findViewById(R.id.btnSpeed);
        btnPip = findViewById(R.id.btnPip);
        btnLock = findViewById(R.id.btnLock);
        btnUnlockFloating = findViewById(R.id.btnUnlockFloating);

        tvVideoTitle.setText(videoTitle != null ? videoTitle : "Green Player");
        tvVideoTitle.setSelected(true);
    }

    private void setupPlayer() {
        if (videoPath == null) {
            Toast.makeText(this, "No video source provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build();

        player = new ExoPlayer.Builder(this)
                .setAudioAttributes(audioAttributes, true)
                .setSeekBackIncrementMs(10000)
                .setSeekForwardIncrementMs(10000)
                .build();

        playerView.setPlayer(player);

        androidx.media3.common.MediaItem mediaItem;
        if (videoPath.startsWith("http://") || videoPath.startsWith("https://") || videoPath.startsWith("content://")) {
            mediaItem = androidx.media3.common.MediaItem.fromUri(Uri.parse(videoPath));
        } else {
            mediaItem = androidx.media3.common.MediaItem.fromUri(Uri.fromFile(new File(videoPath)));
        }

        player.setMediaItem(mediaItem);
        player.prepare();

        // Restore playback position
        long resumePos = resumePrefs.getLong(videoPath, 0);
        if (resumePos > 3000) {
            player.seekTo(resumePos);
        }

        player.setPlayWhenReady(true);

        player.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                btnPlayPause.setImageResource(isPlaying
                        ? android.R.drawable.ic_media_pause
                        : android.R.drawable.ic_media_play);
                if (isPlaying) scheduleAutoHide();
                else cancelAutoHide();
            }

            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY) {
                    long dur = player.getDuration();
                    if (dur > 0) {
                        playerSeekBar.setMax((int) dur);
                        tvDuration.setText(formatTime(dur));
                    }
                } else if (state == Player.STATE_ENDED) {
                    resumePrefs.edit().remove(videoPath).apply();
                }
            }
        });

        // Seekbar updater
        startProgressUpdater();
    }

    private void setupGestures() {
        GestureDetector tapDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (isLocked) {
                    toggleFloatingUnlock();
                } else {
                    toggleControls();
                }
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (isLocked || player == null) return false;
                float x = e.getX();
                int width = gestureTouchView.getWidth();
                if (x < width * 0.35f) {
                    // Rewind 10s
                    long target = Math.max(0, player.getCurrentPosition() - 10000);
                    player.seekTo(target);
                    showToast("-10s ⏪");
                } else if (x > width * 0.65f) {
                    // Forward 10s
                    long target = Math.min(player.getDuration(), player.getCurrentPosition() + 10000);
                    player.seekTo(target);
                    showToast("+10s ⏩");
                } else {
                    // Toggle Play / Pause
                    if (player.isPlaying()) player.pause();
                    else player.play();
                }
                return true;
            }
        });

        gestureTouchView.setOnTouchListener((v, event) -> {
            tapDetector.onTouchEvent(event);
            if (isLocked) return true;

            int width = gestureTouchView.getWidth();
            int height = gestureTouchView.getHeight();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchStartX = event.getX();
                    touchStartY = event.getY();
                    isSwipeVolume = false;
                    isSwipeBrightness = false;
                    isSwipeSeek = false;
                    break;

                case MotionEvent.ACTION_MOVE:
                    float dx = event.getX() - touchStartX;
                    float dy = event.getY() - touchStartY;

                    if (!isSwipeVolume && !isSwipeBrightness && !isSwipeSeek) {
                        if (Math.abs(dy) > GESTURE_THRESHOLD && Math.abs(dy) > Math.abs(dx)) {
                            if (touchStartX > width * 0.5f) {
                                isSwipeVolume = true;
                            } else {
                                isSwipeBrightness = true;
                            }
                        } else if (Math.abs(dx) > GESTURE_THRESHOLD && Math.abs(dx) > Math.abs(dy)) {
                            isSwipeSeek = true;
                            if (player != null) swipeSeekTarget = player.getCurrentPosition();
                        }
                    }

                    if (isSwipeVolume) {
                        handleVolumeSwipe(-dy / height);
                    } else if (isSwipeBrightness) {
                        handleBrightnessSwipe(-dy / height);
                    } else if (isSwipeSeek && player != null) {
                        handleSeekSwipe(dx / width);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    hudVolume.setVisibility(View.GONE);
                    hudBrightness.setVisibility(View.GONE);
                    if (isSwipeSeek && player != null) {
                        hudSeek.setVisibility(View.GONE);
                        player.seekTo(swipeSeekTarget);
                    }
                    scheduleAutoHide();
                    break;
            }
            return true;
        });
    }

    private void handleVolumeSwipe(float delta) {
        if (audioManager == null) return;
        int current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int change = (int) (delta * maxVolume * 2);
        int target = Math.max(0, Math.min(maxVolume, current + change));
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0);

        int percent = (int) ((target / (float) maxVolume) * 100);
        hudVolume.setVisibility(View.VISIBLE);
        pbVolume.setProgress(percent);
        tvVolumePercent.setText(percent + "%");
    }

    private void handleBrightnessSwipe(float delta) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        if (currentBrightness < 0) {
            currentBrightness = lp.screenBrightness;
            if (currentBrightness < 0) currentBrightness = 0.5f;
        }
        currentBrightness = Math.max(0.01f, Math.min(1.0f, currentBrightness + (delta * 0.5f)));
        lp.screenBrightness = currentBrightness;
        getWindow().setAttributes(lp);

        int percent = (int) (currentBrightness * 100);
        hudBrightness.setVisibility(View.VISIBLE);
        pbBrightness.setProgress(percent);
        tvBrightnessPercent.setText(percent + "%");
    }

    private void handleSeekSwipe(float deltaPercent) {
        if (player == null || player.getDuration() <= 0) return;
        long duration = player.getDuration();
        long diff = (long) (deltaPercent * 180000); // Max 3 minutes scrub
        swipeSeekTarget = Math.max(0, Math.min(duration, player.getCurrentPosition() + diff));

        hudSeek.setVisibility(View.VISIBLE);
        String sign = diff >= 0 ? "+" : "-";
        tvSeekDiff.setText(sign + formatTime(Math.abs(diff)));
        tvSeekTargetTime.setText(formatTime(swipeSeekTarget) + " / " + formatTime(duration));
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnPlayPause.setOnClickListener(v -> {
            if (player == null) return;
            if (player.isPlaying()) player.pause();
            else player.play();
            scheduleAutoHide();
        });

        btnRewind.setOnClickListener(v -> {
            if (player != null) {
                player.seekTo(Math.max(0, player.getCurrentPosition() - 10000));
                scheduleAutoHide();
            }
        });

        btnForward.setOnClickListener(v -> {
            if (player != null) {
                player.seekTo(Math.min(player.getDuration(), player.getCurrentPosition() + 10000));
                scheduleAutoHide();
            }
        });

        playerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && player != null) {
                    player.seekTo(progress);
                    tvCurrentPosition.setText(formatTime(progress));
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { cancelAutoHide(); }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { scheduleAutoHide(); }
        });

        btnAspectRatio.setOnClickListener(v -> cycleAspectRatio());
        btnSpeed.setOnClickListener(v -> showSpeedDialog());
        btnAudioTrack.setOnClickListener(v -> showAudioTrackDialog());
        btnSubtitle.setOnClickListener(v -> showSubtitleDialog());
        btnPip.setOnClickListener(v -> enterPipMode());

        btnLock.setOnClickListener(v -> setScreenLock(true));
        btnUnlockFloating.setOnClickListener(v -> setScreenLock(false));
    }

    private void cycleAspectRatio() {
        currentAspectMode = (currentAspectMode + 1) % 4;
        switch (currentAspectMode) {
            case 0:
                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                showToast("Aspect: Fit to Screen 📐");
                break;
            case 1:
                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
                showToast("Aspect: Zoom / Crop ✂️");
                break;
            case 2:
                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                showToast("Aspect: Stretch ↔️");
                break;
            case 3:
                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
                showToast("Aspect: 100% Original 🔍");
                break;
        }
        scheduleAutoHide();
    }

    private void showSpeedDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_speed, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        TextView tvLabel = dialogView.findViewById(R.id.tvCurrentSpeedLabel);
        SeekBar sb = dialogView.findViewById(R.id.sbSpeed);
        float[] speeds = {0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f, 2.5f, 3.0f, 4.0f};

        int currentIdx = 3;
        for (int i = 0; i < speeds.length; i++) {
            if (Math.abs(speeds[i] - currentSpeed) < 0.05f) {
                currentIdx = i;
                break;
            }
        }
        sb.setMax(speeds.length - 1);
        sb.setProgress(currentIdx);
        tvLabel.setText(String.format(Locale.US, "%.2fx %s", speeds[currentIdx], speeds[currentIdx] == 1.0f ? "(Normal)" : ""));

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float s = speeds[progress];
                tvLabel.setText(String.format(Locale.US, "%.2fx %s", s, s == 1.0f ? "(Normal)" : ""));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        dialogView.findViewById(R.id.btnResetSpeed).setOnClickListener(v -> {
            applySpeed(1.0f);
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btnDoneSpeed).setOnClickListener(v -> {
            applySpeed(speeds[sb.getProgress()]);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void applySpeed(float speed) {
        currentSpeed = speed;
        if (player != null) {
            player.setPlaybackParameters(new PlaybackParameters(currentSpeed));
        }
        showToast(String.format(Locale.US, "Speed: %.2fx ⚡", currentSpeed));
        scheduleAutoHide();
    }

    private void showAudioTrackDialog() {
        if (player == null) return;
        Tracks tracks = player.getCurrentTracks();
        List<String> trackNames = new ArrayList<>();
        List<TrackGroup> audioGroups = new ArrayList<>();

        for (Tracks.Group group : tracks.getGroups()) {
            if (group.getType() == C.TRACK_TYPE_AUDIO) {
                TrackGroup tg = group.getMediaTrackGroup();
                for (int i = 0; i < tg.length; i++) {
                    String lang = tg.getFormat(i).language;
                    String label = tg.getFormat(i).label;
                    String name = (label != null ? label : "") + " (" + (lang != null ? lang.toUpperCase() : "Track " + (trackNames.size() + 1)) + ")";
                    trackNames.add(name);
                    audioGroups.add(tg);
                }
            }
        }

        if (trackNames.isEmpty()) {
            showToast("Only 1 audio track available");
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.audio_tracks))
                .setItems(trackNames.toArray(new String[0]), (dialog, which) -> {
                    TrackGroup selectedGroup = audioGroups.get(which);
                    player.setTrackSelectionParameters(player.getTrackSelectionParameters()
                            .buildUpon()
                            .setOverrideForType(new TrackSelectionOverride(selectedGroup, which))
                            .build());
                    showToast("Audio: " + trackNames.get(which));
                })
                .show();
    }

    private void showSubtitleDialog() {
        if (player == null) return;
        Tracks tracks = player.getCurrentTracks();
        List<String> subNames = new ArrayList<>();
        List<TrackGroup> subGroups = new ArrayList<>();

        subNames.add(getString(R.string.subtitle_none));
        subGroups.add(null);

        for (Tracks.Group group : tracks.getGroups()) {
            if (group.getType() == C.TRACK_TYPE_TEXT) {
                TrackGroup tg = group.getMediaTrackGroup();
                for (int i = 0; i < tg.length; i++) {
                    String lang = tg.getFormat(i).language;
                    String label = tg.getFormat(i).label;
                    String name = (label != null ? label : "") + " [" + (lang != null ? lang.toUpperCase() : "Sub " + subNames.size()) + "]";
                    subNames.add(name);
                    subGroups.add(tg);
                }
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.subtitles))
                .setItems(subNames.toArray(new String[0]), (dialog, which) -> {
                    if (which == 0) {
                        player.setTrackSelectionParameters(player.getTrackSelectionParameters()
                                .buildUpon()
                                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                                .build());
                        showToast("Subtitles Off");
                    } else {
                        TrackGroup group = subGroups.get(which);
                        player.setTrackSelectionParameters(player.getTrackSelectionParameters()
                                .buildUpon()
                                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                                .setOverrideForType(new TrackSelectionOverride(group, which - 1))
                                .build());
                        showToast("Subtitle: " + subNames.get(which));
                    }
                })
                .show();
    }

    private void setScreenLock(boolean lock) {
        isLocked = lock;
        if (isLocked) {
            hideControls();
            btnUnlockFloating.setVisibility(View.VISIBLE);
            showToast(getString(R.string.screen_locked));
        } else {
            btnUnlockFloating.setVisibility(View.GONE);
            showControls();
            showToast(getString(R.string.screen_unlocked));
        }
    }

    private void toggleFloatingUnlock() {
        if (btnUnlockFloating.getVisibility() == View.VISIBLE) {
            btnUnlockFloating.setVisibility(View.GONE);
        } else {
            btnUnlockFloating.setVisibility(View.VISIBLE);
            controlsHandler.postDelayed(() -> {
                if (isLocked) btnUnlockFloating.setVisibility(View.GONE);
            }, 3000);
        }
    }

    private void enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            hideControls();
            Rational rational = new Rational(16, 9);
            PictureInPictureParams params = new PictureInPictureParams.Builder()
                    .setAspectRatio(rational)
                    .build();
            enterPictureInPictureMode(params);
        } else {
            showToast("Picture in Picture requires Android 8.0+");
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, @NonNull Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        if (isInPictureInPictureMode) {
            hideControls();
        } else {
            showControls();
        }
    }

    private void toggleControls() {
        if (isControlsShowing) hideControls();
        else showControls();
    }

    private void showControls() {
        controlsContainer.setVisibility(View.VISIBLE);
        isControlsShowing = true;
        scheduleAutoHide();
    }

    private void hideControls() {
        controlsContainer.setVisibility(View.GONE);
        isControlsShowing = false;
        cancelAutoHide();
    }

    private void scheduleAutoHide() {
        cancelAutoHide();
        controlsHandler.postDelayed(autoHideRunnable, 4000);
    }

    private void cancelAutoHide() {
        controlsHandler.removeCallbacks(autoHideRunnable);
    }

    private void startProgressUpdater() {
        controlsHandler.post(new Runnable() {
            @Override
            public void run() {
                if (player != null && player.isPlaying()) {
                    long pos = player.getCurrentPosition();
                    playerSeekBar.setProgress((int) pos);
                    tvCurrentPosition.setText(formatTime(pos));
                }
                controlsHandler.postDelayed(this, 1000);
            }
        });
    }

    private String formatTime(long ms) {
        long totalSeconds = ms / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;
        if (hours > 0) return String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds);
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            if (videoPath != null && player.getCurrentPosition() > 0) {
                resumePrefs.edit().putLong(videoPath, player.getCurrentPosition()).apply();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInPictureInPictureMode()) {
                // Continue in PiP
            } else {
                player.pause();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        controlsHandler.removeCallbacksAndMessages(null);
        if (player != null) {
            if (videoPath != null && player.getCurrentPosition() > 0) {
                resumePrefs.edit().putLong(videoPath, player.getCurrentPosition()).apply();
            }
            player.release();
            player = null;
        }
    }
}
